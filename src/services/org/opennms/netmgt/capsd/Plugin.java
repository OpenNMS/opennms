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
//
// Tab Size = 8
//
//
package org.opennms.netmgt.capsd;

import java.lang.*;
import java.net.InetAddress;
import java.util.Map;

/**
 * <P>The CapsdPlugin interface is the basic interface that a
 * plugin for the capabilites daemon must support. The interface
 * allows the daemon to determine what protocols can be verified
 * by the plugin and has the required methods to verify protocol
 * support for nodes/protocol pairs.</P>
 *
 * @author <A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 *
 */
public interface Plugin
{
	/**
	 * Returns the name of the protocol that this plugin
	 * checks on the target system for support.
	 *
	 * @return The protocol name for this plugin.
	 */
	public String getProtocolName();

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
	public boolean isProtocolSupported(InetAddress address);

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
	public boolean isProtocolSupported(InetAddress address, Map qualifiers);
}

