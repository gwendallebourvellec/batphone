/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.messages;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.meshms.SimpleMeshMS;
import org.servalproject.provider.MessagesContract;
import org.servalproject.servald.Identities;
import org.servalproject.servald.Peer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * activity to show a conversation thread
 *
 */
public class ShowConversationActivity extends ListActivity {

	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private final String TAG = "ShowConversationActivity";

	/*
	 * private class level variables
	 */
	private Cursor cursor;
	private int threadId = -1;

	protected Peer recipient;
	protected final static int DIALOG_RECIPIENT_INVALID = 1;
	private final static int DIALOG_CONTENT_EMPTY = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		if (V_LOG) {
			Log.v(TAG, "on create called");
		}

		// get the thread id from the intent
		Intent mIntent = getIntent();
		String sidString = mIntent.getStringExtra("sid");
		if (sidString != null) {
			// TODO find thread id
			ServalBatPhoneApplication.context
					.displayToastMessage("Todo, find or create conversation thread");
			finish();
		} else {
			threadId = mIntent.getIntExtra("threadId", -1);

			if (threadId == -1) {
				ServalBatPhoneApplication.context
						.displayToastMessage("Unable to open conversation thread");
				finish();
			}
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_conversation);

		Button sendButton = (Button) findViewById(R.id.show_message_ui_btn_send_message);
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView message = (TextView) findViewById(R.id.show_conversation_ui_txt_content);
				if (message.getText() != null && !"".equals(message.getText())) {
					sendMessage(recipient, message.getText().toString());
				} else {
					showDialog(DIALOG_CONTENT_EMPTY);
				}
			}

		});

		Button deleteButton = (Button) findViewById(R.id.delete);
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder b = new AlertDialog.Builder(
						ShowConversationActivity.this);
				b.setMessage("Confirm?");
				b.setNegativeButton("Cancel", null);
				b.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									MessageUtils
											.deleteThread(
													ShowConversationActivity.this,
													threadId);
									ShowConversationActivity.this.finish();
								} catch (Exception e) {
									Log.e("BatPhone", e.getMessage(), e);
									ServalBatPhoneApplication.context
											.displayToastMessage(e.getMessage());
								}
							}

						});
				b.show();
			}

		});
	}

	private void sendMessage(Peer recipient, String text) {
		// send the message
		SimpleMeshMS message = new SimpleMeshMS(
				Identities.getCurrentIdentity(),
				recipient.sid,
				Identities.getCurrentDid(),
				recipient.did,
				System.currentTimeMillis(),
				text
			);
		Intent intent = new Intent("org.servalproject.meshms.SEND_MESHMS");
		intent.putExtra("simple", message);
		startService(intent);
		saveMessage(message);
		// refresh the message list
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				cursor.requery();
			}
		});

	}

	// save the message
	private void saveMessage(SimpleMeshMS message) {
		ContentResolver contentResolver = getContentResolver();
		// save the message
		int[] result = MessageUtils.saveReceivedMessage(message, contentResolver);

		int threadId = result[0];
		int messageId = result[1];

		int toastMessageId;
		if (messageId != -1) {
			Log.i(TAG, "New message saved with messageId '" + messageId
					+ "', threadId '" + threadId + "'");
			toastMessageId = R.string.new_message_ui_toast_sent_successfully;
		} else {
			Log.e(TAG, "unable to save new message");
			toastMessageId = R.string.new_message_ui_toast_sent_unsuccessfully;
		}
		// keep the user informed
		Toast.makeText(getApplicationContext(),
				toastMessageId,
				Toast.LENGTH_LONG).show();
	}

	/*
	 * get the required data and populate the cursor
	 */
	private Cursor populateList() {

		if (V_LOG) {
			Log.v(TAG, "get cursor called");
		}

		// get a content resolver
		ContentResolver mContentResolver = getApplicationContext()
				.getContentResolver();

		Uri mUri = MessagesContract.CONTENT_URI;

		String mSelection = MessagesContract.Table.THREAD_ID + " = ?";
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = Integer.toString(threadId);

		String mOrderBy = MessagesContract.Table.RECEIVED_TIME + " DESC";

		cursor = mContentResolver.query(
				mUri,
				null,
				mSelection,
				mSelectionArgs,
				mOrderBy);

		// zero length arrays required by list adapter constructor,
		// manual matching to views & columns will occur in the bindView method
		String[] mColumnNames = new String[0];
		int[] mLayoutElements = new int[0];

		ShowConversationListAdapter mDataAdapter = new ShowConversationListAdapter(
				this,
				R.layout.show_conversation_item_us,
				cursor,
				mColumnNames,
				mLayoutElements);

		setListAdapter(mDataAdapter);

		return cursor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {

		if (V_LOG) {
			Log.v(TAG, "on pause called");
		}

		// play nice and close the cursor
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {

		if (V_LOG) {
			Log.v(TAG, "on resume called");
		}

		// get the data
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		cursor = populateList();
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {

		if (V_LOG) {
			Log.v(TAG, "on destroy called");
		}

		// play nice and close the cursor if necessary
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}

		super.onDestroy();
	}

	/*
	 * dialog related methods
	 */

	/*
	 * callback method used to construct the required dialog (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		// create the required alert dialog
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		AlertDialog mDialog = null;

		switch (id) {
		case DIALOG_RECIPIENT_INVALID:
			mBuilder.setMessage(
					R.string.new_message_ui_dialog_recipient_invalid)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			mDialog = mBuilder.create();
			break;

		case DIALOG_CONTENT_EMPTY:
			mBuilder.setMessage(R.string.new_message_ui_dialog_content_empty)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			mDialog = mBuilder.create();

			break;
		default:
			mDialog = null;
		}

		return mDialog;
	}

}
