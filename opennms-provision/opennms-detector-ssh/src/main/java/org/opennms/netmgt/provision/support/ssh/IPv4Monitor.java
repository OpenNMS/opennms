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
// 2003 Jan 31: Cleaned up some unused imports.
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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.provision.support.ssh;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.springframework.util.ClassUtils;

/**
 * <p>
 * This class provides a basic implementation for most of the interface methods
 * of the <code>ServiceMonitor</code> class. Since most pollers do not do any
 * special initialization, and only require that the interface is an
 * <code>InetAddress</code> object this class provides eveything by the
 * <code>poll<code> interface.
 *
 * @author <A HREF="mike@opennms.org">Mike</A>
 * @author <A HREF="weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
abstract public class IPv4Monitor implements ServiceMonitor {
    /**
     * <P>
     * This method is called after the framework creates an instance of the
     * plug-in. The framework passes the object a proxy object that can be used
     * to retrieve configuration information specific to the plug-in.
     * Additionally, any parameters for the plug-in from the package definition
     * are passed using the parameters element.
     * </P>
     * 
     * <P>
     * If there is a critical error, like missing service libraries, the the
     * monitor may throw a ServiceMonitorException. If the plug-in throws an
     * exception then the plug-in will be disabled in the framework.
     * </P>
     * @param parameters
     *            Not currently used
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    @SuppressWarnings("unchecked")
    public void initialize(Map parameters) {
    }

    /**
     * <P>
     * This method is called whenever the plug-in is being unloaded, normally
     * during framework exit. During this time the framework may release any
     * resource and save any state information using the proxy object from the
     * initialization routine.
     * </P>
     * 
     * <P>
     * Even if the plug-in throws a monitor exception, it will not prevent the
     * plug-in from being unloaded. The plug-in should not return until all of
     * its state information is saved. Once the plug-in returns from this call
     * its configuration proxy object is considered invalid.
     * </P>
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an error occurs during deallocation.
     * 
     */
    public void release() {
    }

    /**
     * <P>
     * This method is called whenever a new interface that supports the plug-in
     * service is added to the scheduling system. The plug-in has the option to
     * load and/or associate configuration information with the interface before
     * the framework begins scheduling the new device.
     * </P>
     * 
     * <P>
     * Should a monitor exception be thrown during an initialization call then
     * the framework will log an error and discard the interface from
     * scheduling.
     * </P>
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     * @exception
     *                org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException
     *                Thrown if the passed interface is invalid for this
     *                monitor.
     * 
     */
    public void initialize(MonitoredService svc) {
        NetworkInterface iface = svc.getNetInterface();

        if (!(iface.getAddress() instanceof InetAddress))
            throw new NetworkInterfaceNotSupportedException("Address type not supported");

    }

    /**
     * <P>
     * This method is the called whenever an interface is being removed from the
     * scheduler. For example, if a service is determined as being no longer
     * supported then this method will be invoked to cleanup any information
     * associated with this device. This gives the implementor of the interface
     * the ability to serialize any data prior to the interface being discarded.
     * </P>
     * 
     * <P>
     * If an exception is thrown during the release the exception will be
     * logged, but the interface will still be discarded for garbage collection.
     * </P>
     * 
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */
    public void release(MonitoredService svc) {
    }
    
    @SuppressWarnings("unchecked")
    abstract public PollStatus poll(MonitoredService svc, Map parameters);

	protected ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	protected PollStatus logDown(Level level, String reason) {
		return logDown(level, reason, null);
	}

	protected PollStatus logDown(Level level, String reason, Throwable e) {
		String className = ClassUtils.getShortName(getClass());
	    log().debug(className+": "+reason, e);
	    return PollStatus.unavailable(reason);
	}
	
	protected PollStatus logUp(Level level, double responseTime, String logMsg) {
		String className = ClassUtils.getShortName(getClass());
	    log().debug(className+": "+logMsg);
	    return PollStatus.available(responseTime);
	}
}
