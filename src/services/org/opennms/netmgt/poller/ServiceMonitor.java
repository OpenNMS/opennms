//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//

package org.opennms.netmgt.poller;

import java.util.Map;

/**
 * <p>This is the interface that must be implemented by each poller plugin
 * in the framework. This well defined interface allows the framework to
 * treat each plugin identically.</p>

 * <p>When a service monitor plug-in is loaded and initialized, the framework
 * will initialize the monitor by calling the <EM>initialize()</EM> method.
 * Likewise, when the monitor is unloaded the framework calls the <EM>release()</EM>
 * method is called. If the plug-in needs to save or read any configuration
 * information after the initialize() call, a reference to the proxy object should
 * be saved at initialization.</p>
 *
 * <P><STRONG>NOTE:</STRONG> The plug-in <EM>poll()</EM> must be thread safe in
 * order to operate. Any synchronized methods or data accessed in the <EM>poll()</EM>
 * can negatively affect the framework if multiple poller threads are blocked on
 * a critical resource. Synchronization issues should be seriously evaluated to ensure
 * that the plug-in scales well to large deployments.</P>
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public interface ServiceMonitor
{
	/**
	 * <P>The constant that defines a service as being in a normal
	 * state. If this is returned by the poll() method then the 
	 * framework will re-schedule the service for its next poll using
	 * the standard uptime interval</P>
	 */
	public static final int		SERVICE_AVAILABLE	= 1;
	
	/**
	 * <P>The constant that defines a service that is not working
	 * normally and should be scheduled using the downtime models.</P>
	 */
	public static final int		SERVICE_UNAVAILABLE	= 2;
	
	/** 
	 * <P>The constant that defines a service that is up but is
	 * most likely suffering due to excessive load or latency
	 * issues and because of that has not responded within
	 * the configured timeout period.</P>
	 */
	public static final int		SERVICE_UNRESPONSIVE 	= 3;
	
	/**
	 * <P>The status mask is used to mask off the bits that apply to 
	 * scheduling information only. The other bits of the status
	 * return information are used to encode special processing 
	 * instructions to the poller framework.</P>
	 */
	public static final int		SERVICE_STATUS_MASK	= 0xff;
	
	/**
	 * <P>This constants defines the mask that can be bitwise
	 * anded with the status return from the poll() method to 
	 * determine if a status event should be surpressed. By default
	 * the framework will generate an event when the status
	 * transitions from up to down or vice versa. If the mask
	 * bit is set then the framework should not generate a 
	 * transitional event.</P>
	 */
	public static final int		SURPRESS_EVENT_MASK	= 0x100;
	
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
	public void initialize(Map parameters);
		
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
	public void release();
	
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
	public void initialize(NetworkInterface iface);
	
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
	public void release(NetworkInterface iface);
	
	/**
	 * <P>This method is the heart of the plug-in monitor. Each time an interface
	 * requires a check to be performed as defined by the scheduler the poll method
	 * is invoked. The poll is passed the interface to check</P>
	 *
	 * <P>By default when the status transition from up to down or vice versa the 
	 * framework will generate an event. Additionally, if the polling interval 
	 * changes due to an extended unavailbility, the framework will generate an
	 * additional down event. The plug-in can surpress the generation of the default
	 * events by setting the surpress event bit in the returned integer.</P>
	 *
	 * @param iface		The network interface to test the service on.
	 * @param parameters	The package parameters (timeout, retry, etc...) to be 
	 *  used for this poll.
	 *
	 * @return The availibility of the interface and if a transition event
	 * 	should be surpressed.
	 *
	 * @exception java.lang.RuntimeException Thrown if an unrecoverable error
	 *	 occurs that prevents the interface from being monitored.
	 * 
	 * @see #SURPRESS_EVENT_MASK
	 * @see #SERVICE_AVAILABLE
	 * @see #SERVICE_UNAVAILABLE
	 */
	public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg);
}

