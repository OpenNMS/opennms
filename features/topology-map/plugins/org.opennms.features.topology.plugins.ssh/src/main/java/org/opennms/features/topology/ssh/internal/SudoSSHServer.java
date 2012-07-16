package org.opennms.features.topology.ssh.internal;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * This class is can be used to test the client side code
 * by playing the part of the server
 * 
 * @author pdgrenon
 * @author lmbell
 *
 */
@SuppressWarnings("unused")
public class SudoSSHServer extends Thread {

	private InputStream in; // The input stream the pseudo server will read from
	private OutputStream out; // The output stream the pseudo server will write to 
	private OutputStream err; // The error stream the pseudo server will write to
	private int counter;  // Counter used to simulate I/O in to/from the server
	private boolean closed = false; // Tracks whether the wid
	
	/**
	 * constructor for the pseduo server
	 * @param in The input stream to use
	 * @param out The output stream to use 
	 * @param err The error stream to use
	 */
	public SudoSSHServer (InputStream in, OutputStream out, OutputStream err) {
		this.in = in;
		this.out = out;
		this.err = err;
		counter = 0;
	}
	
	/**
	 * Closes the connection to the pseudo server
	 */
	public void close() {
		closed = true;
	}
	/**
	 * Thread used to simulate I/O to/from the server by counting upwards each 
	 * time a new SSH window is open
	 */
	public void run() {
		try {
		while (true) {
			if (closed == true) return;
			StringBuilder fromClient = null;
			if (out != null) {
				
				out.write(("" + counter++ + ", ").getBytes());
				out.flush();
				Thread.sleep(1000);
				
				fromClient = new StringBuilder();
				while (true) {
					int c = in.read();
					if (c == 13) {
						break;
					}
					fromClient.append((char) c);
				}
				if (fromClient.length() > 0) {
					byte[] fromClientBytes = fromClient.toString().getBytes();
					String toClient = "[INFO] Server received: ";
					for (byte b : fromClientBytes){
						toClient += ("[" + b + "]");
					}
					toClient += "\nlmbell@localhost~: ";
					out.write(toClient.getBytes());
					out.flush();
				}
			}
		}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
