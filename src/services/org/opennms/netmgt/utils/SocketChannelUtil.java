package org.opennms.netmgt.utils;

import java.lang.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
/**
 * Class to obtain a connected SocketChannel object.
 *
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a> 
 */ 
public class SocketChannelUtil extends Object
{

    /**
     * This will attempt to connect to the passed host and port.
     * The connection will be made in non-blocking mode, so if there is
     * no route to the host, then it won't hold up capsd or the poller.
     *
     * Once a connection is made, the channel is returned to blocking mode. 
     *
     * @param host	remote host
     * @param port 	port 
     * @param timeout   timeout (ms)
     *
     * @return SocketChannel object already connected to the remote host/port pair.
     */
	public static SocketChannel getConnectedSocketChannel(InetAddress host, int port, int timeout)
		throws IOException, InterruptedException
	{
		SocketChannel sChannel = null;
		
		try
		{
			// try to connect first as non-blocking
			sChannel = SocketChannel.open();
			sChannel.configureBlocking(false);  
			sChannel.connect(new InetSocketAddress(host, port));
			long startConnectTime = System.currentTimeMillis();
			
			// see if connected
			do
			{
				if (!sChannel.finishConnect())
				{
					Thread.sleep(100);
				}
			} while (!sChannel.isConnected() && (System.currentTimeMillis() - startConnectTime) <= timeout);
			
			// check timeout
			if (!sChannel.isConnected())
			{
				if (sChannel.socket() != null)
					sChannel.socket().close();
				
				sChannel.close();
				sChannel = null;
			}
			else
			{
				// we're connected, so return channel to blocking mode. Avoids No Route to Host errors.
				sChannel.configureBlocking(true); 
				sChannel.socket().setSoTimeout(timeout);
			}
		}
		catch (IOException e)
		{
			if (sChannel != null)
				sChannel.close();
			throw e;
		}
		catch (InterruptedException e)
		{
			if (sChannel != null)
				sChannel.close();
			throw e;
		}
		
		return sChannel;
	}
}
