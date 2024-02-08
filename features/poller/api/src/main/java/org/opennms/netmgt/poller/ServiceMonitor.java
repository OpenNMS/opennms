/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
