/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller;

import java.util.Map;

/**
 * <p>
 * This is the interface that must be implemented by each poller plugin in the
 * framework. This well defined interface allows the framework to treat each
 * plugin identically.
 * </p>
 *
 * <p>
 * As of 19.0.0, service monitor plugins are retrieved from a
 * {@link org.opennms.netmgt.poller.ServiceMonitorRegistry}. See the registry
 * implementation documentation for details on how make the service monitors
 * available to the registry.
 * </p>
 *
 * <P>
 * <STRONG>NOTE: </STRONG> The plug-in <EM>poll()</EM> must be thread safe in
 * order to operate. Any synchronized methods or data accessed in the <EM>
 * poll()</EM> can negatively affect the framework if multiple poller threads
 * are blocked on a critical resource. Synchronization issues should be
 * seriously evaluated to ensure that the plug-in scales well to large
 * deployments.
 * </P>
 *
 * @author <A HREF="mailto:jesse@opennms.org">Jesse White</A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public interface ServiceMonitor {

    /**
     * <P>
     * This method is the heart of the plug-in monitor. Each time an interface
     * requires a check to be performed as defined by the scheduler the poll
     * method is invoked. The poll is passed the service to check.
     * </P>
     *
     * <P>
     * By default when the status transition from up to down or vice versa the
     * framework will generate an event. Additionally, if the polling interval
     * changes due to an extended unavailability, the framework will generate an
     * additional down event. The plug-in can suppress the generation of the
     * default events by setting the suppress event bit in the returned integer.
     * </P>
     *
     * <P>
     * <STRONG>NOTE: </STRONG> This method may be invoked on a Minion, in which
     * case certain bean and facilities will not be available. If any state related
     * information is required such as agent related configuration, it should retrieved
     * by the {@link #getRuntimeAttributes(MonitoredService, Map)}.
     * </P>
     *
     * @param svc
     *            Includes details about to the service being monitored.
     * @param parameters
     *            Includes the service parameters defined in <EM>poller-configuration.xml</EM> and those
     *            returned by {@link #getRuntimeAttributes(MonitoredService, Map)}.
     * @return The availability of the interface and if a transition event
     *         should be suppressed.
     * @exception java.lang.RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     * @see PollStatus#SERVICE_AVAILABLE
     * @see PollStatus#SERVICE_UNAVAILABLE
     * @see PollStatus#SERVICE_AVAILABLE
     * @see PollStatus#SERVICE_UNAVAILABLE
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters);

    /**
     *
     * @param svc
     *            Includes details about to the service being monitored.
     * @param parameters
     *            Includes the service parameters defined in <EM>poller-configuration.xml</EM> and those
     *            returned by {@link #getRuntimeAttributes(MonitoredService, Map)}.
     * @return Additional attributes, which should be added to the parameter map before calling {@link #poll(MonitoredService, Map)}.
     */
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters);

    /**
     * Allows the monitor to override the location at which it should be run.
     *
     * @param location
     *            location associated with the service to be monitored
     * @return a possibly updated location
     */
    public String getEffectiveLocation(String location);

}
