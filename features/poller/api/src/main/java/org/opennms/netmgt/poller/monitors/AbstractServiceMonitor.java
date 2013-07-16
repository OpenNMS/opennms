/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

/**
 * <p>
 * This class provides a basic implementation for most of the interface methods
 * of the <code>ServiceMonitor</code> class. Since most pollers do not do any
 * special initialization, and only require that the interface is an
 * <code>InetAddress</code> object this class provides everything by the
 * <code>poll<code> interface.
 *
 * @author <A HREF="mike@opennms.org">Mike</A>
 * @author <A HREF="weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
abstract public class AbstractServiceMonitor implements ServiceMonitor {
	
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceMonitor.class);
	
    /**
     * {@inheritDoc}
     *
     * <P>
     * This method is called after the framework creates an instance of the
     * plug-in. The framework passes the object a proxy object that can be used
     * to retrieve configuration information specific to the plug-in.
     * Additionally, any parameters for the plug-in from the package definition
     * are passed using the parameters element.
     * </P>
     *
     * <P>
     * If there is a critical error, like missing service libraries, the
     * monitor may throw a ServiceMonitorException. If the plug-in throws an
     * exception then the plug-in will be disabled in the framework.
     * </P>
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    @Override
    public void initialize(Map<String, Object> parameters) {
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
     */
    @Override
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
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {}

    /**
     * {@inheritDoc}
     *
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
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */
    @Override
    public void release(MonitoredService svc) {
    }
    
    /** {@inheritDoc} */
    @Override
    abstract public PollStatus poll(MonitoredService svc, Map<String, Object> parameters);
}
