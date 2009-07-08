/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */

// this retrieves data from the deamon so it is not Distribuable
@Distributable(DistributionContext.DAEMON)
public class PassiveServiceMonitor implements ServiceMonitor {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#initialize(org.opennms.netmgt.config.PollerConfig, java.util.Map)
     */
    public void initialize(Map<String, Object> parameters) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#release()
     */
    public void release() {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#initialize(org.opennms.netmgt.poller.MonitoredService)
     */
    public void initialize(MonitoredService svc) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#release(org.opennms.netmgt.poller.MonitoredService)
     */
    public void release(MonitoredService svc) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#poll(org.opennms.netmgt.poller.MonitoredService, java.util.Map, org.opennms.netmgt.config.poller.Package)
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = PassiveStatusKeeper.getInstance().getStatus(svc.getNodeLabel(), svc.getIpAddr(), svc.getSvcName());
        return status;
    }

}
