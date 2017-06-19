/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.opennms.netmgt.poller.support.DefaultServiceMonitorRegistry;
import org.springframework.util.Assert;

/**
 * <p>DefaultPollService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DefaultPollService implements PollService {

    private static final ServiceMonitorRegistry s_serviceMonitorRegistry = new DefaultServiceMonitorRegistry();

    private TimeAdjustment m_timeAdjustment;
    private Map<String, ServiceMonitor> m_monitors = null;

    /**
     * @param timeAdjustment the timeAdjustment to set
     */
    public void setTimeAdjustment(TimeAdjustment timeAdjustment) {
        m_timeAdjustment = timeAdjustment;
    }

    /** {@inheritDoc} */
    @Override
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {

        Map<String, ServiceMonitor> monitors = new HashMap<String, ServiceMonitor>();
        for (ServiceMonitorLocator locator : locators) {
            monitors.put(locator.getServiceName(), locator.getServiceMonitor(s_serviceMonitorRegistry));
        }
        
        m_monitors = monitors;
    }

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(PolledService polledService) {
        ServiceMonitor monitor = getServiceMonitor(polledService);
        if (monitor == null) {
            return PollStatus.unknown("No service monitor for service " + polledService.getSvcName());
        } else {
            PollStatus result = monitor.poll(polledService, polledService.getMonitorConfiguration());
            result.setTimestamp(m_timeAdjustment.adjustDateToMasterDate(result.getTimestamp()));
            return result;
        }
    }

    private ServiceMonitor getServiceMonitor(PolledService polledService) {
        Assert.notNull(m_monitors, "setServiceMonitorLocators must be called before any other operations");
        ServiceMonitor monitor = (ServiceMonitor)m_monitors.get(polledService.getSvcName());
        //Assert.notNull(monitor, "Unable to find monitor for service "+polledService.getSvcName());
        return monitor;
    }

}
