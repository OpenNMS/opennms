//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//  
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software 
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
// 
// For more information contact: 
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
// Tab Stop = 8
//
//
import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;

import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.net.ServerSocket;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public final class SocketTest extends Thread
{
	final static int		MAX_CLIENTS_DEFAULT = 200;
	private static int			m_maxClients;
	
	final static int		NEW_CLIENT_INTERVAL_DEFAULT = 300000;  // 5 minute default
	private static int			m_newClientInterval;
	
	/**
	 * TCP port on which the daemon listens for incoming requests.
	 */
	final static int 		DAEMON_TCP_PORT	= 15000;

	/**
	 * List of clients currently connected to the daemon
	 */
	private static List 	m_clients;
	
	/**
	 * Client thread.
	 */
	private final class Client extends Thread
	{
		int m_count;
		
		int m_lastCheckedCount;
		
		Client(int id)
		{
			super("Client-" + id);
			m_count = 0;
			m_lastCheckedCount = 0;
		}
		
		public boolean statusOk()
		{
			//System.out.println("m_count: " + m_count + " m_lastCheckedCount: " + m_lastCheckedCount);
			if (m_count != m_lastCheckedCount)
			{
				m_lastCheckedCount = m_count;
				return true;
			}
			else
				return false;
		}
		
			
		public void run()
		{
			Socket connection = null;
			DataInputStream dis = null;
			try
			{
				connection = new Socket(InetAddress.getLocalHost(), DAEMON_TCP_PORT);
	
				BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
				dis = new DataInputStream(bis);
			}
			catch(IOException ex)
			{
				if(connection != null)
				{
					try { connection.close(); } catch(Throwable t) { }
				}
				throw new UndeclaredThrowableException(ex);
			}
			catch(Throwable t)
			{
				if(connection != null)
				{
					try { connection.close(); } catch(Throwable tx) { }
				}
				throw new UndeclaredThrowableException(t);
			}
			
			// Read from the server and increment count
			for (;;)
			{
				// Allocate input buffer storage
				byte[] data = new byte[2048];
				
				int len = -1;
				// Read the length of the incoming buffer
				try
				{
					len = dis.readInt();
					//System.out.println(this.getName() + ": num bytes to read: " + len);
				}
				catch(IOException ex)
				{
					if(connection != null)
					{
						try { connection.close(); } catch(Throwable t) { }
					}
					throw new UndeclaredThrowableException(ex);
				}
				
				// Now use length to read data portion of message
				try
				{
					dis.readFully(data, 0, len);
				}
				catch(IOException ex)
				{
					if(connection != null)
					{
						try { connection.close(); } catch(Throwable t) { }
					}
					throw new UndeclaredThrowableException(ex);
				}
				
				// Increment count
				m_count++;
				
				//System.out.println(this.getName() + ": successfully read " + len + " bytes for buffer number " + m_count);
			}
		}
	}

	/**
	 * Server thread.
	 */
	private final class Server extends Thread
	{
		List m_clients;
		
		Server(List clients)
		{
			super("Server");
			m_clients = clients;
		}
		
		public void run()
		{
			// open the server socket
			//
			ServerSocket serverSocket = null;
			try
			{
				serverSocket  = new ServerSocket(DAEMON_TCP_PORT);
			}
			catch(IOException ex)
			{
				throw new UndeclaredThrowableException(ex);
			}

			long lastClientStarted = 0;
			boolean firstTime = true;
		
			// Begin accepting connections from clients
			// For each new client create new Client Handler
			// thread to handle the client's requests.
			//
    			try 
    			{
				serverSocket.setSoTimeout(1000); // Wake up every second 
				
				int client_id = 1;
			
    	  			for(;;)
				{
					// Start a new client every x milliseconds up to the max number of clients
					if ( !firstTime && 
						((System.currentTimeMillis() - lastClientStarted) >= m_newClientInterval) &&
						m_clients.size() < m_maxClients)
					{
						// Create another client
						Client clnt = new Client(client_id);
						clnt.start();
						lastClientStarted = System.currentTimeMillis();
						// add to client list
						m_clients.add(clnt);
					}
				
				
					Socket sock;
					try
					{	
						sock = serverSocket.accept();
					}
					catch (InterruptedIOException iE)
					{
						if (firstTime)
						{
							// create first client
							//System.out.println("SocketTest: creating initial client...");
							Client clnt = new Client(client_id);
							clnt.start();
							firstTime = false;
							lastClientStarted = System.currentTimeMillis();
							// add to client list
							m_clients.add(clnt);
						}
						continue;
					}
	
					// Add the client's new socket connection to the client handler list
					//
					ClientHandler handler = new ClientHandler(client_id, sock);
					handler.start();
				
					System.out.println("SocketTest:  finished starting client & handler pair number " + client_id);
				
					// Increment client id
					client_id++;
      				}
    			}
    			catch (IOException ioE) 
			{
				System.out.println("I/O exception occured processing incomming request " + ioE);
    			}
			catch(Throwable t)
			{
				System.out.println("An undeclared throwable was caught " + t);
			}
			finally
			{
				System.exit(0);
			}
		}
	}
	
	/**
	 * Client handler thread.
	 */
	private final class ClientHandler extends Thread
	{
		Socket m_connection;
		
		ClientHandler(int id, Socket sock)
		{
			super("ClientHandler-" + id);
			m_connection = sock;
		}
		
		public void run()
		{
			DataOutputStream dos = null;
			try
			{
				BufferedOutputStream bos = new BufferedOutputStream(m_connection.getOutputStream());
				dos = new DataOutputStream(bos);
			}
			catch(IOException ex)
			{
				if(m_connection != null)
				{
					try { m_connection.close(); } catch(Throwable t) { }
				}
				throw new UndeclaredThrowableException(ex);
			}
			
			int written = 0;
			byte[] sendBuf = new byte[2048];
			Random generator = new Random();
			
			for (;;)
			{
				// Allocate output buffer storage

				// Send the buffer
				int len = -1;
				try
				{
					// Randomly generate number between 1 and 2048
					// using (random_number mod 2048)
					len = generator.nextInt(2048);
					if (len == 0)
						len = 2048;
					//System.out.println(this.getName() + ": calculated bytes to send: " + len);
					
					// first send length of data 
					dos.writeInt(len);
					dos.flush();
					
					
					// now send the data
					dos.write(sendBuf, 0, len);
					dos.flush();
				}
				catch(IOException ex)
				{
					if(m_connection != null)
					{
						try { m_connection.close(); } catch(Throwable t) { }
					}
					throw new UndeclaredThrowableException(ex);
				}
				
				// Increment count
				written++;
				
				//System.out.println(this.getName() + ": successfully sent " + len + " bytes for buffer number: " + written);
				
				try
				{
					sleep(1);
				}
				catch (InterruptedException ie)
				{
					throw new UndeclaredThrowableException(ie);
				}
			}
		}
	}
	
	/**
	 *
	 */
	public SocketTest()
	{
		m_clients = null;
		m_maxClients = MAX_CLIENTS_DEFAULT;
		m_newClientInterval =  NEW_CLIENT_INTERVAL_DEFAULT;
	}

	/**
	 *
	 */
	public synchronized void init()
	{
		// the client list
		//
		m_clients = Collections.synchronizedList(new LinkedList());
		
		// start the server
		//
		Server server = new Server(m_clients);
		server.start();
	}

	/**
	 * The main routine. Basically a watchdog thread which is responsible
	 * for verifying that none of the server/client sockets are hung.
	 */
	public void run()
	{
		int count = 0;
		
		for (;;)
		{
			try
			{
				sleep(2000);
			}
			catch(InterruptedException ie)
			{
				throw new UndeclaredThrowableException(ie);
			}
						
			// Go through client list and verify that
			// none are hung by checking their read counts.
			boolean hungClient = false;
			synchronized(m_clients)
			{
				Iterator iter = m_clients.iterator();
				while (iter.hasNext())
				{
					Client clnt = (Client)iter.next();
					if (!clnt.statusOk())
					{
						System.out.println("SocketTest: client '" + clnt.getName() + "' appears to be hung!");
						hungClient = true;
						break;
					}
				}
			}
			
			count++;
			
			if ((count % 10 == 0) && !hungClient)
			{
				System.out.println("SocketTest: " + m_clients.size() + "  clients...status: ok.");
			}
		}
	}
	
	public static void main(String[] args)
	{
		// Start socket test
		SocketTest tester = new SocketTest();
		
		// Any command line args?
		int index = 0;
		System.out.println("SocketTest: parsing args...");
		while (index < args.length)
		{
			// Max clients
			if (args[index].equals("-c"))
			{
				String str = args[index+1];
				try
				{
					m_maxClients = Integer.parseInt(str);
				}
				catch (NumberFormatException nfE)
				{
					System.out.println("SocketTest: number format exception parsing max clients argument...defaulting to " + MAX_CLIENTS_DEFAULT);
					m_maxClients = MAX_CLIENTS_DEFAULT;
				}
			}
			
			// New interval
			if (args[index].equals("-i"))
			{
				String str = args[index+1];
				try
				{
					m_newClientInterval = Integer.parseInt(str);
				}
				catch (NumberFormatException nfE)
				{
					System.out.println("SocketTest: number format exception parsing new client interval argument...defaulting to " + NEW_CLIENT_INTERVAL_DEFAULT + "ms");
					m_newClientInterval = NEW_CLIENT_INTERVAL_DEFAULT;
				}
			}
			
			index = index+2;
		}
		
		System.out.println("Initializing tester...maxClients: " + m_maxClients + " newClientInterval: " + m_newClientInterval);
		tester.init();
		System.out.println("Starting tester...");
		tester.start();
		System.out.println("Finished main...");
	}
}
