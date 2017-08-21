/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.ssh.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractComponent;

/**
 * The SSHTerminal class is a custom Vaadin component that emulates VT100
 * terminals and connects remotely to servers via SSH
 * @author lmbell
 * @author pdgrenon
 */
public class SSHTerminal extends AbstractComponent {


	private static final long serialVersionUID = -8914800725736485264L; // serialization ID
	private int TERM_WIDTH;  // The width of the terminal
	private int TERM_HEIGHT;  // The height of the terminal
	private boolean forceUpdate; // Tracks whether the client should be forced to update 
	private boolean focus; // Tells the client to focus on itself
	private boolean isClosed;  // Tracks whether the whether is closed
	private boolean closeClient;  // Boolean sent from the server to close the client
	private SessionTerminal st;  // The terminal specific to the current session
	private ClientSession session;  // The client instance used in the authorization of user names and passwords 
	private String dumpContents; // The content from the server to be displayed by the client
	private SSHWindow sshWindow;  // The window that holds the terminal
	private ClientChannel channel;  // The connection between the client and the server

	/**
	 * Constructor for the SSH Terminal 
	 * @param sshWindow The window holding the terminal
	 * @param session The client instance used in the authorization of user names and passwords
	 * @param width The width of the terminal
	 * @param height The height of the terminal
	 */
	public SSHTerminal(SSHWindow sshWindow, ClientSession session, int width, int height) {
		super();
		this.sshWindow = sshWindow;
		this.session = session;
		TERM_WIDTH = width;
		TERM_HEIGHT = height;
		dumpContents = null;
		closeClient = false;
		isClosed = false;
		try {
			st = new SessionTerminal();
			forceUpdate = true;
			focus = false;
		} catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
	}

	/**
	 * Closes the client window
	 */
	public boolean close() {
		closeClient = true;
		requestRepaint();
		return closeClient;
	}

	@Override
	protected SSHTerminalState getState() {
	    return (SSHTerminalState) super.getState();
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
                LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
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
				return terminal.dump();
			} catch (InterruptedException e) {
                LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
				throw new InterruptedIOException(e.toString());
			}
		}

		/**
		 * Runs the terminal and reads/writes when necessary
		 */
                @Override
		public void run() {
			try {
				for (;;) {
					byte[] buf = new byte[8192];
					int l = out.read(buf);
					InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(buf, 0, l));
					final StringBuilder sb = new StringBuilder();
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
                LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
			}
		}
		
	}

}
