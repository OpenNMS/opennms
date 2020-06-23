/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors.nrpe;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * <p>CheckNrpe class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class CheckNrpe {
	/** Constant <code>DEFAULT_PORT=5666</code> */
	public static final int DEFAULT_PORT = 5666;
	/** Constant <code>DEFAULT_TIMEOUT=10</code> */
	public static final int DEFAULT_TIMEOUT = 10;
	
	private static final String s_usage =
		"Usage: java CheckNrpe -H <host> [-p <port>] [-P <padding>] \\\n" +
		"                      [-t <timeout>] [-c <command>] [-a <arglist ...>]\n" +
		"Note: if the -a option is specified it *must* be the last option\n";
	
	
	/**
	 * <p>executeQuery</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 * @param buffer a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.poller.nrpe.NrpePacket} object.
	 * @throws java.lang.Exception if any.
	 */
	public static NrpePacket executeQuery(String host, String buffer) throws Exception {
		return executeQuery(host, DEFAULT_PORT, buffer,
				NrpePacket.DEFAULT_PADDING);
	}
	
	/**
	 * <p>executeQuery</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 * @param buffer a {@link java.lang.String} object.
	 * @param padding a int.
	 * @return a {@link org.opennms.netmgt.poller.nrpe.NrpePacket} object.
	 * @throws java.lang.Exception if any.
	 */
	public static NrpePacket executeQuery(String host, String buffer, int padding) throws Exception {
		return executeQuery(host, DEFAULT_PORT, buffer, padding);
	}
	
	/**
	 * <p>executeQuery</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 * @param port a int.
	 * @param buffer a {@link java.lang.String} object.
	 * @param padding a int.
	 * @return a {@link org.opennms.netmgt.poller.nrpe.NrpePacket} object.
	 * @throws java.lang.Exception if any.
	 */
	public static NrpePacket executeQuery(String host, int port, String buffer,
			int padding) throws Exception {
		NrpePacket p = new NrpePacket(NrpePacket.QUERY_PACKET, (short) 0,
				buffer);
		byte[] b = p.buildPacket(padding);
		Socket s = new Socket(host, port);
		OutputStream o = s.getOutputStream();
		o.write(b);
		
		try {
			return NrpePacket.receivePacket(s.getInputStream(), padding);
		} finally {
			s.close();
		}
	}
	
	/**
	 * <p>sendPacket</p>
	 *
	 * @param type a short.
	 * @param resultCode a short.
	 * @param buffer a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.poller.nrpe.NrpePacket} object.
	 * @throws java.lang.Exception if any.
	 */
	public static NrpePacket sendPacket(short type, short resultCode, String buffer) throws Exception {
		int padding = NrpePacket.DEFAULT_PADDING;
		
		NrpePacket p = new NrpePacket(type, resultCode, buffer);
		byte[] b = p.buildPacket(padding);
		Socket s = new Socket("localhost", DEFAULT_PORT);
		OutputStream o = s.getOutputStream();
		o.write(b);
		
		try {
			return NrpePacket.receivePacket(s.getInputStream(), padding);
		} finally {
			s.close();
		}
	}
	
	/**
	 * <p>main</p>
	 *
	 * @param argv an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] argv) throws Exception {
		String host = null;
		int port = DEFAULT_PORT;
		int padding = NrpePacket.DEFAULT_PADDING;
		@SuppressWarnings("unused")
        int timeout = DEFAULT_TIMEOUT;
		String command = NrpePacket.HELLO_COMMAND;
		LinkedList<String> arglist = new LinkedList<>();
		
		for (int i = 0; i < argv.length; i++) {
			if (argv[i].equals("-h")) {
				System.out.print(s_usage);
				System.exit(0);
			} else if (argv[i].equals("-H")) {
				host = nextArg(argv, ++i);
			} else if (argv[i].equals("-p")) {
				port = Integer.parseInt(nextArg(argv, ++i));
			} else if (argv[i].equals("-P")) {
				padding = Integer.parseInt(nextArg(argv, ++i));
			} else if (argv[i].equals("-t")) {
				timeout = Integer.parseInt(nextArg(argv, ++i));
			} else if (argv[i].equals("-c")) {
				command = nextArg(argv, ++i);
			} else if (argv[i].equals("-a")) {
				arglist.add(nextArg(argv, ++i));
			} else if (argv[i].startsWith("-")) {
				throw new Exception("Unknown option \"" + argv[i] + "\".  " +
				"Use \"-h\" option for help.");
			} else {
				throw new Exception("No non-option arguments are allowed.  " +
				"Use \"-h\" option for help.");
			}
		}
		
		if (host == null) {
			throw new Exception("You must specify a -H option.  " +
			"Use \"-h\" option for help.");
		}
		
		final StringBuilder buffer = new StringBuilder();
		buffer.append(command);
		for (Iterator<String> i = arglist.iterator(); i.hasNext(); ) {
			buffer.append(" ");
			buffer.append(i.next());
		}
		
		// XXX still need to do something with the timeout
		NrpePacket p = executeQuery(host, port, buffer.toString(), padding);
		System.out.println(p.getBuffer());
		System.exit(p.getResultCode());
	}
	
	/**
	 * <p>nextArg</p>
	 *
	 * @param argv an array of {@link java.lang.String} objects.
	 * @param i a int.
	 * @return a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	public static String nextArg(String[] argv, int i) throws Exception {
		if (i >= argv.length) {
			throw new Exception("No more command-line arguments but option " +
			"requires an argument.  Use \"-h\" for help.");
		}
		return argv[i];
	}
}
