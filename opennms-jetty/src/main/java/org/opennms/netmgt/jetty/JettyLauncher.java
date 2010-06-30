/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.jetty;

import java.io.File;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Properties;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.start.Monitor;

/**
 * Launches a WAR file on port 8980.
 *
 * @author Seth
 * @version $Id: $
 */
public class JettyLauncher {

	private static final int STOP_PORT = 8981;
	// Random string that should make it harder for a random user to shut down Jetty
	private static final String STOP_KEY = "b7f3609ab1dd8c7e5bac070bca9ba0d5";

	/**
	 * <p>usage</p>
	 */
	public static void usage() {
		System.out.println("To launch a WAR file on port 8980:");
		System.out.println("  org.opennms.netmgt.jetty.JettyLauncher <war_file_path>");
		System.out.println("To check the status of the Jetty process:");
		System.out.println("  org.opennms.netmgt.jetty.JettyLauncher STATUS");
		System.out.println("To stop the Jetty process:");
		System.out.println("  org.opennms.netmgt.jetty.JettyLauncher STOP");
	}

	/**
	 * <p>main</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("ERROR: Incorrect number of arguments.");
			usage();
			System.exit(1);
		}

		if ("STOP".equals(args[0])) {
			Socket s = new Socket(InetAddress.getByName("127.0.0.1"), STOP_PORT);
			OutputStream out = s.getOutputStream();
			out.write((STOP_KEY + "\r\n").getBytes("ASCII"));
			out.write(("stop" + "\r\n").getBytes("ASCII"));
			out.flush();
			s.close();
		} else if ("STATUS".equals(args[0])) {
			Socket s = null;
			String statusResponse = null;
			try {
				s = new Socket(InetAddress.getByName("127.0.0.1"), STOP_PORT);
				OutputStream out = s.getOutputStream();
				LineNumberReader reader = new LineNumberReader(new InputStreamReader(s.getInputStream(), "ASCII"));
				out.write((STOP_KEY + "\r\n").getBytes("ASCII"));
				out.write(("status" + "\r\n").getBytes("ASCII"));
				out.flush();

				if ("OK".equals(reader.readLine())) {
					System.exit(0);
				} else {
					System.exit(1);
				}
			} catch (Throwable e) {
				System.exit(1);
			} finally {
				if (s != null) { 
					s.close();
				}
			}
		} else {
			// Enable Jetty debug logging
			Server server = new Server();
			Connector connector = new SelectChannelConnector();
			connector.setPort(8980);
			server.addConnector(connector);
			Handler handler = new WebAppContext(args[0], "/");
			server.addHandler(handler);
			server.start();
			
			// Start a Jetty shutdown monitor thread too
			System.setProperty("STOP.PORT", String.valueOf(STOP_PORT));
			System.setProperty("STOP.KEY", STOP_KEY);
			Monitor.monitor();
		}
	}
}
