/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.util.Map;

import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IcmpMonitorWrapper class.
 * 
 * <p>A wrapper of the IcmpMonitor implementation.</p>
 * 
 * <p>If the IP Address of the MonitoredService starts with 169.254,
 * the PassiveStatusMonitor will be used instead of the IcmpMonitor to
 * perform the test.</p>
 * 
 * <p>The path-outage feature only allows to define a single parent per node.
 * For this reason, an operator can define a virtual node with a fake IP, and
 * use that IP as the critical path for the dependent nodes. Then, the user
 * can define a logic to control the status of the fakeIp through the PSK.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Distributable(DistributionContext.DAEMON)
public class IcmpMonitorWrapper extends AbstractServiceMonitor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(IcmpMonitorWrapper.class);

    /** The ICMP monitor. */
    private ServiceMonitor icmpMonitor;

    /** The PSK monitor. */
    private ServiceMonitor pskMonitor;

    /**
     * Instantiates a new ICMP monitor wrapper.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public IcmpMonitorWrapper() throws IOException {
        icmpMonitor = new IcmpMonitor();
        pskMonitor = new PassiveServiceMonitor();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.monitors.AbstractServiceMonitor#poll(org.opennms.netmgt.poller.MonitoredService, java.util.Map)
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        ServiceMonitor svcMonitor = svc.getIpAddr().startsWith("169.254.") ? pskMonitor : icmpMonitor;
        LOG.debug("Polling {}@{} using {}", svc.getSvcName(), svc.getIpAddr(), svcMonitor.getClass().getSimpleName());
        return svcMonitor.poll(svc, parameters);
    }

}
