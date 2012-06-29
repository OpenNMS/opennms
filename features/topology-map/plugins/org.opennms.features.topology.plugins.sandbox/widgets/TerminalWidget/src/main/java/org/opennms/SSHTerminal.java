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
import java.util.Map;

import org.opennms.gwt.client.ui.VTerminal;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

@SuppressWarnings("serial")
@ClientWidget(VTerminal.class)
public class SSHTerminal extends AbstractComponent {

	private int TERM_WIDTH;
	private int TERM_HEIGHT;
	private SessionTerminal st;
	private String dumpContents;

	public SSHTerminal(int width, int height) {
		super();
		TERM_WIDTH = width;
		TERM_HEIGHT = height;
		dumpContents = null;
		try {
			st = new SessionTerminal();
		} catch (IOException e) { e.printStackTrace(); }
	}

	/** Paint (serialize) the component for the client. */
	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		// Superclass writes any common attributes in the paint target.
		super.paintContent(target);

		// Add the currently selected color as a variable in the paint
		// target.
		target.addVariable(this, "fromSSH", dumpContents); //TODO add dump contents to variable
	}

	/** Deserialize changes received from client. */
	@SuppressWarnings("rawtypes")
	@Override
	public void changeVariables(Object source, Map variables) {
		if (variables.containsKey("toSSH") && !isReadOnly()) {
			final String bytesToSSH = (String) variables.get("toSSH");
			try {
				if (st == null || st.isClosed()) {
					st = new SessionTerminal();
				}
				dumpContents = st.handle(bytesToSSH, true);
				requestRepaint();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}

	public class SessionTerminal implements Runnable {

		private Terminal terminal;
		private PipedOutputStream in;
		private PipedInputStream out;
		private boolean closed;

		public SessionTerminal() throws IOException {
			try {
				this.terminal = new Terminal(TERM_WIDTH, TERM_HEIGHT);
				terminal.write("\u001b\u005B20\u0068"); // set newline mode on
				terminal.write("lmbell@localhost~: ");
				in = new PipedOutputStream();
				out = new PipedInputStream();
				PrintStream pipedOut = new PrintStream(new PipedOutputStream(out), true);
				PipedInputStream pipedIn = new PipedInputStream(in);
				SudoSSHServer sshServer = new SudoSSHServer(pipedIn, pipedOut, pipedOut);
				new Thread(sshServer).start();
				//TODO start SSH session and pass in streams

			} catch (Exception e) {
				e.printStackTrace();
			}
			new Thread(this).start();
		}

		public boolean isClosed() {
			return closed;
		}

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
				closed = true;
				throw e;
			}
			try {
				return terminal.dump(10, forceDump);
			} catch (InterruptedException e) {
				throw new InterruptedIOException(e.toString());
			}
		}

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
				closed = true;
				e.printStackTrace();
			}
		}

	}

}
