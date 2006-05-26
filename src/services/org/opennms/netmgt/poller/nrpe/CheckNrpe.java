package org.opennms.netmgt.poller.nrpe;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class CheckNrpe {
	public static final int DEFAULT_PORT = 5666;
	public static final int DEFAULT_TIMEOUT = 10;
	
	private static final String s_usage =
		"Usage: java CheckNrpe -H <host> [-p <port>] [-P <padding>] \\\n" +
		"                      [-t <timeout>] [-c <command>] [-a <arglist ...>]\n" +
		"Note: if the -a option is specified it *must* be the last option\n";
	
	
	public static NrpePacket executeQuery(String host, String buffer) throws Exception {
		return executeQuery(host, DEFAULT_PORT, buffer,
				NrpePacket.DEFAULT_PADDING);
	}
	
	public static NrpePacket executeQuery(String host, String buffer, int padding) throws Exception {
		return executeQuery(host, DEFAULT_PORT, buffer, padding);
	}
	
	public static NrpePacket executeQuery(String host, int port, String buffer,
			int padding) throws Exception {
		NrpePacket p = new NrpePacket(NrpePacket.QUERY_PACKET, (short) 0,
				buffer);
		byte[] b = p.buildPacket(padding);
		Socket s = new Socket(host, port);
		OutputStream o = s.getOutputStream();
		o.write(b);
		
		return NrpePacket.receivePacket(s.getInputStream(), padding);
	}
	
	public static NrpePacket sendPacket(short type, short resultCode, String buffer) throws Exception {
		int padding = NrpePacket.DEFAULT_PADDING;
		
		NrpePacket p = new NrpePacket(type, resultCode, buffer);
		byte[] b = p.buildPacket(padding);
		Socket s = new Socket("localhost", DEFAULT_PORT);
		OutputStream o = s.getOutputStream();
		o.write(b);
		
		return NrpePacket.receivePacket(s.getInputStream(), padding);
	}
	
	public static void main(String[] argv) throws Exception {
		String host = null;
		int port = DEFAULT_PORT;
		int padding = NrpePacket.DEFAULT_PADDING;
		int timeout = DEFAULT_TIMEOUT;
		String command = NrpePacket.HELLO_COMMAND;
		LinkedList arglist = new LinkedList();
		
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
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(command);
		for (Iterator i = arglist.iterator(); i.hasNext(); ) {
			buffer.append(" ");
			buffer.append((String) i.next());
		}
		
		// XXX still need to do something with the timeout
		NrpePacket p = executeQuery(host, port, buffer.toString(), padding);
		System.out.println(p.getBuffer());
		System.exit(p.getResultCode());
	}
	
	public static String nextArg(String[] argv, int i) throws Exception {
		if (i >= argv.length) {
			throw new Exception("No more command-line arguments but option " +
			"requires an argument.  Use \"-h\" for help.");
		}
		return argv[i];
	}
}
