package org.opennms;

import java.io.InputStream;
import java.io.OutputStream;

public class SudoSSHServer extends Thread {

	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	private int counter;
	private boolean closed = false;
	
	public SudoSSHServer (InputStream in, OutputStream out, OutputStream err) {
		this.in = in;
		this.out = out;
		this.err = err;
		counter = 0;
	}
	
	public void close() {
		closed = true;
	}

	public void run() {
		try {
		while (true) {
			if (closed == true) return;
			StringBuilder fromClient = null;
			if (out != null) {
				
				out.write(("" + counter++ + ", ").getBytes());
				out.flush();
				Thread.sleep(1000);
				
//				fromClient = new StringBuilder();
//				while (true) {
//					int c = in.read();
//					if (c == 13) {
//						break;
//					}
//					fromClient.append((char) c);
//				}
//				if (fromClient.length() > 0) {
//					byte[] fromClientBytes = fromClient.toString().getBytes();
//					String toClient = "[INFO] Server received: ";
//					for (byte b : fromClientBytes){
//						toClient += ("[" + b + "]");
//					}
//					toClient += "\nlmbell@localhost~: ";
//					out.write(toClient.getBytes());
//					out.flush();
//				}
			}
		}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
