/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.tl1d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Category;

public class Tl1ClientImpl implements Tl1Client {

	public class TimeoutSleeper {

		public void sleep() throws InterruptedException {
			Thread.sleep(3000);
		}

	}

	String m_host = "127.0.0.1";
	int m_port = 502;
	boolean m_started = false;
	
	private Socket m_tl1Socket;
    private Thread m_socketReader;
	private BlockingQueue<Tl1Message> m_tl1Queue;
	private BufferedReader m_reader;
	private TimeoutSleeper m_sleeper;
	private Category m_log;


	public Tl1ClientImpl(BlockingQueue<Tl1Message> queue, Category log) {
		this("localhost", 15000, queue);
		m_log = log;
	}
	
	public Tl1ClientImpl(String host, int port, BlockingQueue<Tl1Message> queue) {
		m_host = host;
		m_port = port;
		m_tl1Queue = queue;
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#start()
	 */
	public void start() {
		m_log.info("Starting TL1 client: "+m_host+":"+String.valueOf(m_port));
		m_started = true;
		
		m_socketReader = new Thread("TL1-Socket-Reader") {
			
			public void run() {
				readMessages();
			}

		};
		
		m_socketReader.start();
		m_log.info("Started TL1 client: "+m_host+":"+String.valueOf(m_port));
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#stop()
	 */
	public void stop() {
		m_log.info("Stopping TL1 client: "+m_host+":"+String.valueOf(m_port));
		m_started = false;
	}
	
	private BufferedReader getReader() {
		if (m_reader == null) {
			m_reader = createReader();
		}
		return m_reader;
	}
	
	private BufferedReader createReader() {
		BufferedReader reader;
		while (m_started) {
		    try {
				m_tl1Socket = new Socket(m_host, m_port);
				reader = new BufferedReader(new InputStreamReader(m_tl1Socket.getInputStream()));
				resetTimeout();
				return reader;
			} catch (IOException e) {
				e.printStackTrace();
				waitUntilNextConnectTime();
			} 
		}
		return null;
	}

	private void resetTimeout() {
		m_sleeper = null;
	}

	private void waitUntilNextConnectTime() {
		if (m_started) {
			if (m_sleeper == null) {
				m_sleeper = new TimeoutSleeper();
			}
			try { m_sleeper.sleep(); } catch (InterruptedException e) { }
		}
	}

	private void readMessages() {
		StringBuilder rawMessage = new StringBuilder();
		m_log.debug("readMessages: Begin reading off socket...");
		while(m_started) {
			try {
				m_log.info("readMessages: reading line from TL1 socket...");
				BufferedReader reader = getReader();
				if (reader != null) {
					int ch;
					while((ch = reader.read()) != -1) {
						rawMessage.append((char)ch);
						if((char)ch == ';') {
							m_log.debug("readMessages: offering message to queue: "+rawMessage.toString());
							m_tl1Queue.offer(Tl1Message.create(rawMessage.toString()));
							m_log.debug("readMessages: successfully offered to queue.");
							rawMessage.setLength(0);
						}
					}
					m_log.warn("readMessages: resetting socket reader to client: "+m_host+":"+m_port);
					resetReader(null);
				}
			} catch (IOException e) {
				resetReader(e);
			}
		}
		m_log.info("Stopping TL1 client: "+m_host+":"+String.valueOf(m_port));
	}

	private void resetReader(IOException ex) {
		if (ex != null) {
			ex.printStackTrace();
		}
		try {
			m_reader.close();
		} catch (IOException e) { 
		} finally {
			m_reader = null;
		}
		try {
			m_tl1Socket.close();
		} catch (IOException e) {
			m_tl1Socket = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#getHost()
	 */
	public String getHost() {
		return m_host;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#setHost(java.lang.String)
	 */
	public void setHost(String host) {
		m_host = host;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#getPort()
	 */
	public int getPort() {
		return m_port;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#setPort(int)
	 */
	public void setPort(int port) {
		m_port = port;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#getTl1Socket()
	 */
	public Socket getTl1Socket() {
		return m_tl1Socket;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#setTl1Socket(java.net.Socket)
	 */
	public void setTl1Socket(Socket tl1Socket) {
		m_tl1Socket = tl1Socket;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#getSocketReader()
	 */
	public Thread getSocketReader() {
		return m_socketReader;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.tl1d.Tl1Client#setSocketReader(java.lang.Thread)
	 */
	public void setSocketReader(Thread socketReader) {
		m_socketReader = socketReader;
	}

}
