/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.rrd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.opennms.netmgt.rrd.tcp.PerformanceDataProtos;

public class PerfDataReceiver {

	private volatile static Thread m_listenerThread;

	public static void main(String[] args) {
		Thread listener = null;

		Runtime.getRuntime().addShutdownHook(createShutdownHook());

		int port = 8999;
		
		if (args.length < 1) {
			System.err.println("Defaulting to port: 8999.  To change, pass valid port value as first argument.");
		} else {
			port = Integer.valueOf(args[0]);
		}
		
		System.err.println("Ready to receive OpenNMS QOS Data on TCP Port:"+String.valueOf(port)+"...");
		try {
			listener = createListenerThread(port);
			listener.start();
			listener.join();
		} catch (Throwable t) {
			System.err.print(t.getLocalizedMessage() + "\n\n" + t);
		}
		
	}
	
	public static Thread createShutdownHook() {
		Thread t = new Thread() {
			@Override
			public void run() {
				System.out.println("\nHave a nice day! :)");
				Runtime.getRuntime().halt(0);
			}
		};
		return t;
	}

	public static Thread createListenerThread(final int port) {
		m_listenerThread = new Thread() {
			public void run() {
				this.setName("fail");
				try {
					ServerSocket ssocket = new ServerSocket(port);
					ssocket.setSoTimeout(0);
					while (true) {
						try {
							Socket socket = ssocket.accept();
							InputStream is = socket.getInputStream();
							PerformanceDataProtos.PerformanceDataReadings messages = PerformanceDataProtos.PerformanceDataReadings.parseFrom(is);
							for (PerformanceDataProtos.PerformanceDataReading message : messages.getMessageList()) {
								StringBuffer values = new StringBuffer();
								values.append("{ ");
								for (int i = 0; i < message.getValueCount(); i++) {
									if (i != 0) {
										values.append(", ");
									}
									values.append(message.getValue(i));
								}
								values.append(" }");
								System.out
										.println("Message received: { "
												+ "path: \""
												+ message.getPath() + "\", "
												+ "owner: \""
												+ message.getOwner() + "\", "
												+ "timestamp: \""
												+ message.getTimestamp()
												+ "\", " + "values: "
												+ values.toString() + " }");

							}
						} catch (SocketTimeoutException e) {
							System.err.println(e.getLocalizedMessage());
							if (this.isInterrupted()) {
								System.err.println("Interrupted.");
								this.setName("notfailed");
								return;
							}
						} catch (IOException e) {
							System.err.println(e.getLocalizedMessage());
						}
					}
					
					
				} catch (IOException e) {
					System.err.println(e.getLocalizedMessage());
				} catch (Throwable e) {
					System.err.println(e.getLocalizedMessage());
				}
			}
		};
		
		return m_listenerThread;

	}
}
