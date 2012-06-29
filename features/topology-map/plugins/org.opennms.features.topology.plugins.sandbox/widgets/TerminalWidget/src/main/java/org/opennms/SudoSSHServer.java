package org.opennms;

import java.io.InputStream;
import java.io.OutputStream;

public class SudoSSHServer implements Runnable {

	private InputStream in;
	private OutputStream out;
	private OutputStream err;
	
	public SudoSSHServer (InputStream in, OutputStream out, OutputStream err) {
		this.in = in;
		this.out = out;
		this.err = err;
	}

	public void run() {
		try {
		while (true) {
			StringBuilder fromClient = null;
			if (in != null) {
				fromClient = new StringBuilder();
				while (true) {
					int c = in.read();
					if (c == -1) {
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
				}
			}
		}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
