//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
//
package org.opennms.netmgt.capsd;

import java.lang.*;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import jcifs.netbios.NbtAddress;


/**
 * <P>This class is designed to be used by the capabilities
 * daemon to test for SMB support on remote interfaces.
 * The class implements the Plugin interface that allows 
 * it to be used along with other plugins by the daemon.</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A HREF="mailto:weave@opennms.org">Weave</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 *
 */
public final class SmbPlugin
	extends AbstractPlugin
{
	/**
	 * The protocol that this plugin checks for.
	 */
	private final static String 	PROTOCOL_NAME = "SMB";

	/**
	 * <P>Test to see if the passed host talks SMB &amp; has a NetBIOS name</P>
	 *
	 * @param host	The remote host to check
	 *
	 * @return True if the remote interface responds talks SMB and has a
	 *        NETBIOS name.  False otherwise.
	 */
	private boolean isSmb(InetAddress host)
	{
		Category log = ThreadCategory.getInstance(getClass());
		boolean isAServer = false;
		try
		{
			NbtAddress nbtAddr = NbtAddress.getByName(host.getHostAddress());
			
			// If the retrieved SMB name is equal to the IP address
			// of the host, the it is safe to assume that the interface
			// does not support SMB
			//
			if(nbtAddr.getHostName().equals(host.getHostAddress()))
			{
				if (log.isDebugEnabled())
					log.debug("SmbPlugin: failed to retrieve SMB name for " + host.getHostAddress());
			}
			else
			{
				isAServer = true;
			}
		}
		catch(UnknownHostException e)
		{
			if (log.isDebugEnabled())
				log.debug("SmbPlugin: UnknownHostException: " + e.getMessage());
		}
		catch(IOException e)
		{
			log.info("SmbPlugin: An unexpected I/O exception occured checking host " + host.getHostAddress(), e);
		}
		catch(Throwable t)
		{
			log.error("SmbPlugin: An undeclared throwable exception was caught checking host " + host.getHostAddress(), t);
		}	
			
		return isAServer;
	}

	/**
	 * Returns the name of the protocol that this plugin
	 * checks on the target system for support.
	 *
	 * @return The protocol name for this plugin.
	 */
	public String getProtocolName()
	{
		return PROTOCOL_NAME;
	}

	/**
	 * Returns true if the protocol defined by this
	 * plugin is supported. If the protocol is not 
	 * supported then a false value is returned to the 
	 * caller.
	 *
	 * @param address	The address to check for support.
	 *
	 * @return True if the protocol is supported by the address.
	 */
	public boolean isProtocolSupported(InetAddress address)
	{
		return isSmb(address);
	}

	/**
	 * Returns true if the protocol defined by this
	 * plugin is supported. If the protocol is not 
	 * supported then a false value is returned to the 
	 * caller. The qualifier map passed to the method is
	 * used by the plugin to return additional information
	 * by key-name. These key-value pairs can be added to 
	 * service events if needed.
	 *
	 * @param address	The address to check for support.
	 * @param qualiier	The map where qualification are set
	 *			by the plugin.
	 *
	 * @return True if the protocol is supported by the address.
	 */
	public boolean isProtocolSupported(InetAddress address, Map qualifiers)
	{
		return isSmb(address);
	}

}

