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
//	Brian Weaver	<weave@oculan.com>
//	http://www.opennms.org/
//
// Tab Stop = 8
//
//
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class InetEchoXchange
{
	static class BufferXchange
	{
		private static final int RECV = 0;
		private static final int SENT = 1;

		private byte[] m_sent;
		private int m_state;

		BufferXchange()
		{
			this.m_sent = null;
			this.m_state= RECV;
		}

		synchronized boolean isReceived()
		{
			return this.m_state == RECV;
		}

		synchronized boolean isSent()
		{
			return this.m_state == SENT;
		}

		synchronized void setRecipt()
		{
			this.m_state = RECV;
			this.notifyAll();
		}

		synchronized void setSent(byte[] buffer)
		{
			this.m_state = SENT;
			this.m_sent  = buffer;
			this.notifyAll();
		}

		synchronized byte[] getBuffer()
		{
			return this.m_sent;
		}
	}

	static class Sender
		extends Thread
	{
		private DataOutputStream m_stream;
		private BufferXchange m_xchange;
		private Random m_rand;
		private int m_monitor;

		Sender(BufferXchange xchange, OutputStream outs)
		{
			super("Sender" + (System.currentTimeMillis() / 1000) % (24*3600));
			m_xchange = xchange;
			m_stream = new DataOutputStream(new BufferedOutputStream(outs));
			m_rand = new Random(System.currentTimeMillis());
			m_monitor = 0;
		}

		synchronized int getMonitor()
		{
			return m_monitor;
		}

		public void run()
		{
			for(;;)
			{
				int len = m_rand.nextInt(4096);
				byte[] data = new byte[len];
				m_rand.nextBytes(data);

				// send the data now
				//
				synchronized(m_xchange)
				{
					while(!m_xchange.isReceived())
					{
						try
						{
							m_xchange.wait();
						}
						catch(InterruptedException e)
						{
							e.printStackTrace();
							return;
						}
					}

					try
					{
						m_stream.writeInt(len);
						m_stream.write(data, 0, data.length);
						m_stream.flush();
					}
					catch(IOException ioE)
					{
						ioE.printStackTrace();
						return;
					}

					m_xchange.setSent(data);
				}

				synchronized(this)
				{
					m_monitor++;
				}
			}
		}
	}

	static class Receiver
		extends Thread
	{
		private DataInputStream m_stream;
		private BufferXchange m_xchange;
		private int m_monitor;
	
		Receiver(BufferXchange xchange, InputStream ins)
		{
			super("Receiver" + (System.currentTimeMillis() / 1000) % (24*3600));
			m_stream = new DataInputStream(new BufferedInputStream(ins, 8192));
			m_xchange = xchange;
			m_monitor = 0;
		}

		synchronized int getMonitor()
		{
			return m_monitor;
		}

		public void run()
		{
			for(;;)
			{
				synchronized(m_xchange)
				{
					while(!m_xchange.isSent())
					{
						try
						{
							m_xchange.wait();
						}
						catch(InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					
					byte[] compdata = m_xchange.getBuffer();

					try
					{
						int len = m_stream.readInt();
						if(len != compdata.length)
						{
							System.out.println("Invalid Stream Marker (" + len + " != "  + compdata.length + ")");
							return;
						}

						byte[] data = new byte[len];
						m_stream.readFully(data);

						for(int i = 0; i < data.length; i++)
						{
							if(data[i] != compdata[i])
							{
								System.out.println("Invalid Stream Data");
								return;
							}
						}
						m_xchange.setRecipt();
					}
					catch(IOException ioE)
					{
						ioE.printStackTrace();
						return;
					}
				}

				synchronized(this)
				{
					m_monitor++;
				}

			}
		}
	}

	static class Pair
	{
		private Object m_first;
		private Object m_second;

		Pair(Object first, Object second)
		{
			m_first = first;
			m_second = second;
		}

		Object getFirst()
		{
			return m_first;
		}

		Object getSecond()
		{
			return m_second;
		}
	}

	public static void main(String[] args)
	{
		long now   = System.currentTimeMillis();
		long start = 0;

		LinkedList srlist = new LinkedList();
		LinkedList cntlist = new LinkedList();


		for(;;)
		{
			now = System.currentTimeMillis();
			if((now - start) > 60000 && srlist.size() < 128) // five minutes have passed
			{
				start = System.currentTimeMillis();
				System.out.println("Starting new echo thread");
				try
				{
					Socket s = new Socket("127.0.0.1", 7);
					s.setTcpNoDelay(true);
					BufferXchange xchange = new BufferXchange();
					Receiver recv = new Receiver(xchange, s.getInputStream());
					Sender snd = new Sender(xchange, s.getOutputStream());

					srlist.add(new Pair(snd, recv));
					cntlist.add(new Pair(new Integer(0), new Integer(0)));

					recv.start();
					snd.start();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				Thread.sleep(10000);
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}

			//
			// check the senders and receivers
			//
			for(int x = 0; x < srlist.size(); x++)
			{
				Pair a = (Pair) srlist.get(x);
				Pair b = (Pair) cntlist.get(x);

				int old = ((Integer)b.getFirst()).intValue();
				int y = ((Sender)a.getFirst()).getMonitor();

				if(y == old)
				{
					System.out.println("Count has not changed for " + a.getFirst());
					continue;
				}

				old = ((Integer)b.getSecond()).intValue();
				int z   = ((Receiver)a.getSecond()).getMonitor();

				if(z == old)
				{
					System.out.println("Count has not changed for " + a.getSecond());
					continue;
				}

				cntlist.set(x, new Pair(new Integer(y), new Integer(z)));
			}

		} // end infinite loop
	}
}

