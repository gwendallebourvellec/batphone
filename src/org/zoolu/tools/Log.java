/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.tools;

import java.io.PrintStream;


// import java.util.Locale;
// import java.text.DateFormat;
// import java.text.SimpleDateFormat;

/**
 * Class Log allows the printing of log messages onto standard output or files
 * or any PrintStream.
 * <p>
 * Every Log has a <i>verboselevel</i> associated with it; any log request with
 * <i>loglevel</i> less or equal to the <i>verbose-level</i> is logged. <br>
 * Verbose level 0 indicates no log. The log levels should be greater than 0.
 * <p>
 * Parameter <i>logname</i>, if non-null, is used as log header (i.e. written
 * at the begin of each log row).
 */
public class Log {


	public Log() {}
	public Log(PrintStream out_stream, String log_tag, int verbose_level) {}
	public Log(String file_name, String log_tag, int verbose_level) {}
	public Log(String file_name, String log_tag, int verbose_level,
			long max_size) {}
	public Log(String file_name, String log_tag, int verbose_level,
			long max_size, boolean append) {}

	/** Initializes the log */
	protected void init(PrintStream out_stream, String log_tag,
			int verbose_level, long max_size) {}

	/** Flushes */
	protected Log flush() {
		return this;
	}

	/** *************************** Public methods **************************** */

	/** Closes the log */
	public void close() {}

	/** Logs the Exception */
	public Log printException(Exception e, int level) { // ByteArrayOutputStream
		// err=new
		// ByteArrayOutputStream();
		// e.printStackTrace(new PrintStream(err));
		// return println("Exception: "+err.toString(),level);
		android.util.Log.v("SipDroid", e.toString(), e);
		return this;
	}

	/** Logs the Exception.toString() and Exception.printStackTrace() */
	public Log printException(Exception e) {
		return printException(e, 1);
	}

	/** Logs the packet timestamp */
	public Log printPacketTimestamp(String proto, String remote_addr,
			int remote_port, int len, String message, int level) {
		String str = remote_addr + ":" + remote_port + "/" + proto + " (" + len
				+ " bytes)";
		if (message != null)
			str += ": " + message;
		println(str, level);
		return this;
	}

	/**
	 * Prints the <i>log</i> if <i>level</i> isn't greater than the Log
	 * <i>verbose_level</i>
	 */
	public Log println(String message, int level) {
		return print(message + "\r\n", level).flush();
	}

	/** Prints the <i>log</i> if the Log <i>verbose_level</i> is greater than 0 */
	public Log println(String message) {
		return println(message, 1);
	}

	/** Prints the <i>log</i> if the Log <i>verbose_level</i> is greater than 0 */
	public Log print(String message) {
		return print(message, 1);
	}

	/**
	 * Prints the <i>log</i> if <i>level</i> isn't greater than the Log
	 * <i>verbose_level</i>
	 */
	public Log print(String message, int level) {
		android.util.Log.v("SipDroid",message);
		return this;
	}

}