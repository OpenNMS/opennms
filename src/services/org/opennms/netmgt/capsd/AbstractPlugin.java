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

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>This class provides a basic implementation for most of the interface
 * methods of the <code>ServiceMonitor</code> class. Since most pollers do not
 * do any special initialization, and only require that the interface is an
 * <code>InetAddress</code> object this class provides eveything by the
 * <code>poll<code> interface.
 *
 * @author <A HREF="mike@opennms.org">Mike</A>
 * @author <A HREF="weave@opennms.org">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
abstract class AbstractPlugin
	implements Plugin
{
	/**
	 * This method is used to lookup a specific key in
	 * the map. If the mapped value is a string is is converted
	 * to an interger and the original string value is replaced
	 * in the map. The converted value is returned to the caller.
	 * If the value cannot be converted then the default value is
	 * used.
	 *
	 * @return The int value associated with the key.
	 */
	final static int getKeyedInteger(Map map, String key, int defValue)
	{
		int value = defValue;
		Object oValue = map.get(key);

		if(oValue != null && oValue instanceof String)
		{
			try
			{
				value = Integer.parseInt((String)oValue);
			}
			catch(NumberFormatException ne)
			{
				value = defValue;
				ThreadCategory.getInstance(AbstractPlugin.class).info("getKeyedInteger(): Failed to convert value " + oValue + " for key " + key);
			}
			map.put(key, new Integer(value));
		}
		else if(oValue != null)
		{
			value = ((Integer)oValue).intValue();
		}
		return value;
	}

	/**
	 * This method is used to lookup a specific key in 
	 * the map. If the mapped value is a string is is converted
	 * to an interger and the original string value is replaced
	 * in the map. The converted value is returned to the caller.
	 * If the value cannot be converted then the default value is
	 * used.
	 *
	 * @return The int value associated with the key.
	 */
	final static String getKeyedString(Map map, String key, String defValue)
	{
		String value = defValue;
		Object oValue = map.get(key);

		if(oValue != null && oValue instanceof String)
		{
			value = (String)oValue;
		}
		else if(oValue != null)
		{
			value = oValue.toString();
			map.put(key, value);
		}
		return value;
	}

	/**
	 * Returns the name of the protocol that this plugin
	 * checks on the target system for support.
	 *
	 * @return The protocol name for this plugin.
	 */
	public abstract String getProtocolName();

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
	public abstract boolean isProtocolSupported(InetAddress address);

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
	public abstract boolean isProtocolSupported(InetAddress address, Map qualifiers);

}

