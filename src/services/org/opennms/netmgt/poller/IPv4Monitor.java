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
package org.opennms.netmgt.poller;

import java.lang.*;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
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
abstract class IPv4Monitor
	implements ServiceMonitor
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
				ThreadCategory.getInstance(IPv4Monitor.class).info("getIntByKey: Failed to convert value " + oValue + " for key " + key);
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
	 * <P>This method is called after the framework creates an
	 * instance of the plug-in. The framework passes the object a proxy
	 * object that can be used to retreive configuration information 
	 * specific to the plug-in. Additionally, any parameters for the 
	 * plug-in from the package definition are passed using the 
	 * parameters element.</P>
	 *
	 * <P>If there is a critical error, like missing service libraries, the
	 * the montior may throw a ServiceMonitorException. If the plug-in 
	 * throws an exception then the plug-in will be disabled in the
	 * framework.</P>
	 *
	 * @param parameters	Not currently used
	 *
	 * @exception java.lang.RuntimeException Thrown if
	 * 	an unrecoverable error occurs that prevents the plug-in from functioning.
	 *
	 */
	public void initialize(Map parameters) 
	{
		return;
	}
		
	/**
	 * <P>This method is called whenever the plug-in is being unloaded, normally
	 * during framework exit. During this time the framework may release any 
	 * resource and save any state information using the proxy object from the
	 * initialization routine.</P>
	 *
	 * <P>Even if the plug-in throws a monitor exception, it will not prevent
	 * the plug-in from being unloaded. The plug-in should not return until all
	 * of its state information is saved. Once the plug-in returns from this 
	 * call its configuration proxy object is considered invalid.</P>
	 *
	 * @exception java.lang.RuntimeException Thrown if an error occurs
	 * 	during deallocation.
	 *
	 */
	public void release() 
	{
		return;
	}
	
	/**
	 * <P>This method is called whenever a new interface that supports the 
	 * plug-in service is added to the scheduling system. The plug-in has the
	 * option to load and/or associate configuration information with the
	 * interface before the framework begins scheduling the new device.</P>
	 *
	 * <P>Should a monitor exception be thrown during an initialization call
	 * then the framework will log an error and discard the interface from 
	 * scheduling.</P>
	 *
	 * @param iface		The network interface to be added to the scheduler.
	 *
	 * @exception java.lang.RuntimeException Thrown if an unrecoverable error
	 *	 occurs that prevents the interface from being monitored.
	 * @exception org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException Thrown
	 * 	if the passed interface is invalid for this monitor.
	 *
	 */
	public void initialize(NetworkInterface iface) 
	{
		if(!(iface.getAddress() instanceof InetAddress))
			throw new NetworkInterfaceNotSupportedException("Address type not supported");
	 	return;
	}
	
	/**
	 * <P>This method is the called whenever an interface is being removed from 
	 * the scheduler. For example, if a service is determined as being no longer
	 * supported then this method will be invoked to cleanup any information 
	 * associated with this device. This gives the implementor of the interface
	 * the ability to serialize any data prior to the interface being discarded.</P>
	 *
	 * <P>If an exception is thrown during the release the exception will be
	 * logged, but the interface will still be discarded for garbage collection.</P>
	 *
	 * @param iface		The network interface that was being monitored.
	 *
	 * @exception java.lang.RuntimeException Thrown if an unrecoverable error
	 *	 occurs that prevents the interface from being monitored.
	 */
	public void release(NetworkInterface iface) 
	{
		return;
	}
}

