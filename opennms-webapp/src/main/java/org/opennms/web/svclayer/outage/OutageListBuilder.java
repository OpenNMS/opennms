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

package org.opennms.web.svclayer.outage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

/**
 * <p>OutageListBuilder class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @since 1.8.1
 */
public class OutageListBuilder {

    /**
     * <p>theTable</p>
     *
     * @param foundOutages a {@link java.util.Collection} object.
     * @return a {@link java.util.List} object.
     */
    public List<Map<String, Object>> theTable(Collection<OnmsOutage> foundOutages) {
        List<Map<String, Object>> theTable = new ArrayList<Map<String, Object>>();

        for (OnmsOutage outage : foundOutages) {
            OnmsMonitoredService monitoredService = outage.getMonitoredService();
            OnmsServiceType serviceType = monitoredService.getServiceType();
            OnmsIpInterface ipInterface = monitoredService.getIpInterface();

            // ips.put(outage.getId(), ipInterface.getIpAddress());
            // nodes.put(outage.getId(), ipInterface.getNode().getLabel());
            // nodeids.put(outage.getId(), monitoredService.getNodeId());
            // services.put(outage.getId(), serviceType.getName());

            Map<String, Object> outagerow = new HashMap<String, Object>();
            outagerow.put("outage", outage);
            outagerow.put("outageid", outage.getId());
            outagerow.put("node", ipInterface.getNode().getLabel());
            outagerow.put("nodeid", monitoredService.getNodeId());
            outagerow.put("ipaddr", InetAddressUtils.str(ipInterface.getIpAddress()));
            outagerow.put("interfaceid", ipInterface.getId());
            outagerow.put("ifserviceid", monitoredService.getId());
            outagerow.put("service", serviceType.getName());
            outagerow.put("serviceid", serviceType.getId());
            outagerow.put("eventid", outage.getServiceLostEvent().getId());

            // if (outage.getIfLostService() != null) {
            // outagerow.put("down",
            // formatter.format(outage.getIfLostService()));
            // }

            if (outage.getIfLostService() != null) {
                outagerow.put("iflostservice", outage.getIfLostService());
                outagerow.put("iflostservicelong", outage.getIfLostService().getTime());
            }

            if (outage.getIfRegainedService() != null) {
                outagerow.put("ifregainedservice", outage.getIfRegainedService());
                outagerow.put("ifregainedservicelong", outage.getIfRegainedService().getTime());
            }

            if (outage.getSuppressTime() != null) {
                outagerow.put("suppresstime", outage.getSuppressTime());
            }

            outagerow.put("suppressedby", outage.getSuppressedBy());

            // Build a droplist for this outage

            theTable.add(outagerow);
        }

        return theTable;

    }

}
