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
package org.opennms.netmgt.poller;

import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.io.*;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.Interface;

/**
 * <p>This class provides a basic implementation for most of the interface
 * methods of the <code>ServiceMonitor</code> class in addition to methods
 * for creating and updating RRD files with latency information. Since most pollers 
 * do not do any special initialization, and only require that the interface is an
 * <code>InetAddress</code> object this class provides eveything by the
 * <code>poll<code> interface.
 *
 * @author <A HREF="mike@opennms.org">Mike</A>
 * @author <A HREF="weave@opennms.org">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
abstract class IPv4LatencyMonitor
	implements ServiceMonitor
{
	/**
	 * Interface object which provides access to RRD functions via JNI.
	 */
	Interface m_rrdInterface;
	
	/** 
	 * RRD data source name which doubles as the RRD file name.
	 */
	static String DS_NAME = "response-time";
	
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
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		// Initialize jni RRD interface.
		// 
		try
		{
			org.opennms.netmgt.rrd.Interface.init();
		}
		catch(SecurityException se)
		{
			log.fatal("initialize: Failed to initialize JNI RRD interface", se);
			throw new UndeclaredThrowableException(se);
		}
		catch(UnsatisfiedLinkError ule)
		{
			log.fatal("initialize: Failed to initialize JNI RRD interface", ule);
			throw new UndeclaredThrowableException(ule);
		}
		
		// Save local reference to singleton instance 
		//
		m_rrdInterface = org.opennms.netmgt.rrd.Interface.getInstance();
		if (log.isDebugEnabled())
			log.debug("initialize: successfully instantiated JNI interface to RRD...");
		
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
	
	/**
	 * Create an RRD database file for storing latency/response time
	 * data.
	 * 
	 * @param rrdJniInterface 	interface used to issue RRD commands.
	 * @param repository		path to the RRD file repository
	 * @param addr			interface address
	 * @param dsName		data source/RRD file name
	 * 
	 * @return true if RRD file successfully created, false otherwise
	 */
	public boolean createRRD(Interface rrdJniInterface, String repository, InetAddress addr, String dsName)
	{
		Category log = ThreadCategory.getInstance(this.getClass());
		
		// add interface address to RRD repository path
		String path = repository + File.separator + addr.getHostAddress();
		
		// If a directory does not yet exist for this interface create it.
		//
		File f = new File(path);
		if (!f.isDirectory())
			if (!f.mkdirs())
				throw new RuntimeException("Unable to create RRD file repository, path: " + path);
		
		// add RRD file name to path
		String fullPath = path + File.separator + dsName + ".rrd";
		
		f = new File(fullPath);
		if (f.exists())
		{
			// Already exists, no need to create
			return false;
		}
		else
		{
			// Build RRD create command
			// 
			// Step size: 	5 minutes
			// RRAs:    	1 week of 5 minute average data
			//              6 months worth of hourly min, max, average data
			String cmd = "create " + fullPath + " --step 300 " +
				"DS:" + dsName + ":GAUGE:600:U:U " + 
				"RRA:AVERAGE:0.5:1:2016 " +
				"RRA:AVERAGE:0.5:12:4464 " + 
				"RRA:MIN:0.5:12:4464 " +
				"RRA:MAX:0.5:12:4464";
			
			if (log.isDebugEnabled())
				log.debug("createRRD: issuing RRD create command: " + cmd);
			
			// Issue the RRD 'create' command
			String[] results = rrdJniInterface.launch(cmd);
		    
			if (log.isDebugEnabled())
				log.debug("createRRD: RRD create command completed for " + addr.getHostAddress());
				
			// Sanity check results array
			if (results == null)
			{
				if(log.isEnabledFor(Priority.ERROR))
				{
					log.error("createRRD: Unexpected failure calling native method launch() with command string: " + cmd);
					log.error("createRRD: No error text available.");
				}
				throw new RuntimeException("RRD database 'create' failed for interface " + addr.getHostAddress());
			}
		    
			// Check error string at index 0, will be null if create was successful
			if (results[0] != null)
			{
				if(log.isEnabledFor(Priority.ERROR))
					log.error("RRD database 'create' failed for " + addr.getHostAddress() + ", reason: " + results[0]);
					throw new RuntimeException("RRD database 'create' failed for interface " + addr.getHostAddress() + ", reason: " + results[0]);
			}
	
			return true;
		}
		
	}
	
	/**
	 * Update an RRD database file with latency/response time data.
	 * 
	 * @param rrdJniInterface 	interface used to issue RRD commands.
	 * @param repository		path to the RRD file repository
	 * @param addr			interface address
	 * @param value			value to update the RRD file with
	 * 
	 * @return true if RRD file successfully created, false otherwise
	 */
	public void updateRRD(Interface rrdJniInterface, String repository, InetAddress addr, String dsName, long value)
	{
		Category log = ThreadCategory.getInstance(this.getClass());
		
		// Create RRD if it doesn't already exist
		createRRD(rrdJniInterface, repository, addr, dsName);
		
		// Build complete path
		String fullPath = repository + File.separator + addr.getHostAddress() + File.separator + dsName + ".rrd";
		
		// update RRD database
		String cmd = "update " + fullPath + " N:" + String.valueOf(value);
		
		if (log.isDebugEnabled())
			log.debug("updateRRD: issuing RRD update command: " + cmd);

		// Issue the RRD 'create' command
		String[] results = rrdJniInterface.launch(cmd);
		
		if (log.isDebugEnabled())
			log.debug("updateRRD: RRD update command completed for " + addr.getHostAddress());
			
		// Sanity check results array
		if (results == null)
		{
			if(log.isEnabledFor(Priority.ERROR))
			{
				log.error("updateRRD: Unexpected failure calling native method launch() with command string: " + cmd);
				log.error("updateRRD: No error text available.");
			}
			throw new RuntimeException("RRD database 'update' failed for interface " + addr.getHostAddress());
		}
	    
		// Check error string at index 0, will be null if create was successful
		if (results[0] != null)
		{
			if(log.isEnabledFor(Priority.ERROR))
				log.error("RRD database 'update' failed for " + addr.getHostAddress() + ", reason: " + results[0]);
				throw new RuntimeException("RRD database 'update' failed for interface " + addr.getHostAddress() + ", reason: " + results[0]);
		}
	}
}

