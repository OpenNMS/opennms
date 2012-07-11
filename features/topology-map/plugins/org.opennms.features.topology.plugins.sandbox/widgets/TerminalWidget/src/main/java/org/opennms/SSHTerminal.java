/* 
 * Copyright 2009 IT Mill Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opennms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import com.Ostermiller.util.NoCloseInputStream;
import com.Ostermiller.util.NoCloseOutputStream;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.opennms.gwt.client.ui.VTerminal;

import com.vaadin.Application;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Window;

/**
 * 
 * 
 * @author lmbell
 * @author pdgrenon
 *
 */
@ClientWidget(VTerminal.class)
public class SSHTerminal extends AbstractComponent {

	private static final long serialVersionUID = -8914800725736485264L; // serialization ID
	private int TERM_WIDTH;  // The width of the terminal
	private int TERM_HEIGHT;  // The height of the terminal 
	private SessionTerminal st;  // The terminal specific to the current session
	private ClientSession session;  // The client instance used in the authorization of user names and passwords 
	private String dumpContents; // The content from the server to be displayed by the client
	private boolean forceUpdate = false;  // Tracks whether the client should be forced to update 
	private String isClosed;  // Tracks whether the whether is closed
	private String closeClient;  // Boolean sent from the server to close the client
	private Application app;  // The main application
	private SSHWindow sshWindow;  // The window that holds the terminal
	private ClientChannel channel;  // The connection between the client and the server

	/**
	 * Constructor for the SSH Terminal 
	 * @param app The main application
	 * @param sshWindow The window holding the terminal
	 * @param session The client instance used in the authorization of user names and passwords
	 * @param width The width of the terminal
	 * @param height The height of the terminal
	 */
	public SSHTerminal(TerminalApplication app, SSHWindow sshWindow, ClientSession session, int width, int height) {
		super();
		TERM_WIDTH = width;
		TERM_HEIGHT = height;
		dumpContents = null;
		this.session = session;
		closeClient = "false";
		this.app = app;
		this.sshWindow = sshWindow;
		try {
			isClosed = "false";
			st = new SessionTerminal();
			forceUpdate = true;

		} catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * Closes the client window
	 */
	public void close() {
		closeClient = "true";
		requestRepaint();
	}

	/** Paint (serialize) the component for the client. */
	@Override
	public synchronized void paintContent(PaintTarget target) throws PaintException {
		// Superclass writes any common attributes in the paint target.
		super.paintContent(target);

		// Add the currently selected color as a variable in the paint
		// target.
		target.addVariable(this, "fromSSH", dumpContents);
		target.addVariable(this, "update", forceUpdate);
		target.addVariable(this, "closeClient", closeClient);
		forceUpdate = false;
	}

	/** Deserialize changes received from client. */
	@SuppressWarnings("rawtypes")
	@Override
	public synchronized void changeVariables(Object source, Map variables) {
		if (variables.containsKey("isClosed")) {
			isClosed = ((String)variables.get("isClosed"));
			if (isClosed.equals("true")){
				channel.close(true);
				app.getMainWindow().removeWindow(sshWindow);
				requestRepaint();
			}
		}
		if (variables.containsKey("toSSH") && !isReadOnly()) {
			final String bytesToSSH = (String) variables.get("toSSH");
			try {
				if (st == null) {
					st = new SessionTerminal();
				}
				dumpContents = st.handle(bytesToSSH, true);
				requestRepaint();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}

	/**
	 * Nested class used to create the client side terminal
	 * 
	 * @author pdgrenon
	 * @author lmbell
	 */
	public class SessionTerminal implements Runnable {

		private Terminal terminal;  // The terminal to be displayed
		private NoClosePipedOutputStream in;  // The input stream to be used by the terminal
		private NoClosePipedInputStream out;  // The output stream to be used by the terminal

		/**
		 * Constructor that creates creates the terminal and
		 * connects the I/O streams to the server
		 * @throws IOException
		 */
		public SessionTerminal() throws IOException {
			try {
				this.terminal = new Terminal(TERM_WIDTH, TERM_HEIGHT);
				in = new NoClosePipedOutputStream();
				out = new NoClosePipedInputStream();
				NoClosePipedOutputStream pipedOut = new NoClosePipedOutputStream(out);
				NoClosePipedInputStream pipedIn = new NoClosePipedInputStream(in);
				channel = session.createChannel(ClientChannel.CHANNEL_SHELL);
				channel.setIn(pipedIn);
				channel.setOut(pipedOut);
				channel.setErr(pipedOut);
				new Thread(this).start();
				channel.open();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Handles the content recieved from the server
		 * 
		 * @param str The content recieved
		 * @param forceDump Whether the terminal is forced to dump the content
		 * @return The contents dumped to terminal
		 * @throws IOException
		 */
		public String handle(String str, boolean forceDump) throws IOException {
			try {
				if (str != null && str.length() > 0) {
					String d = terminal.pipe(str);
					for (byte b : d.getBytes()) {
						in.write(b);
					}
					in.flush();
				}
			} catch (IOException e) {
				throw e;
			}
			try {
				return terminal.dump(10, forceDump);
			} catch (InterruptedException e) {
				throw new InterruptedIOException(e.toString());
			}
		}

		/**
		 * Runs the terminal and reads/writes when necessary
		 */
		public void run() {
			try {
				for (;;) {
					byte[] buf = new byte[8192];
					int l = out.read(buf);
					InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(buf, 0, l));
					StringBuilder sb = new StringBuilder();
					for (;;) {
						int c = r.read();
						if (c == -1) {
							break;
						}
						sb.append((char) c);
					}
					if (sb.length() > 0) {
						terminal.write(sb.toString());
					}
					String s = terminal.read();
					if (s != null && s.length() > 0) {
						for (byte b : s.getBytes()) {
							in.write(b);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
