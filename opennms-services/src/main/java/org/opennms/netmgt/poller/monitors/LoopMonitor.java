/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.plugins.LoopPlugin;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
/**
 * <p>LoopMonitor class.</p>
 *
 * @author david
 * @version $Id: $
 */

@Distributable
public class LoopMonitor implements ServiceMonitor {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#initialize(org.opennms.netmgt.config.PollerConfig, java.util.Map)
     */
    /** {@inheritDoc} */
    @Override
    public void initialize(Map<String, Object> parameters) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#release()
     */
    /**
     * <p>release</p>
     */
    @Override
    public void release() {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#initialize(org.opennms.netmgt.poller.MonitoredService)
     */
    /**
     * <p>initialize</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#release(org.opennms.netmgt.poller.MonitoredService)
     */
    /** {@inheritDoc} */
    @Override
    public void release(MonitoredService svc) {
        return;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.ServiceMonitor#poll(org.opennms.netmgt.poller.MonitoredService, java.util.Map, org.opennms.netmgt.config.poller.Package)
     */
    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        LoopPlugin lp = new LoopPlugin();
        boolean isAvailable = lp.isProtocolSupported(svc.getAddress(), parameters);
        int status = (isAvailable ? PollStatus.SERVICE_AVAILABLE : PollStatus.SERVICE_UNAVAILABLE);
        StringBuffer sb = new StringBuffer();
        sb.append("LoopMonitor configured with is-supported =  ");
        sb.append(ParameterMap.getKeyedString(parameters, "is-supported", "false"));
        sb.append(" for ip-match: ");
        sb.append(ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*"));
        
        return PollStatus.get(status, sb.toString());
    }

}
