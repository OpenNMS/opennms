/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Dec 12: Format code, use Java 5 generics. - dj@opennms.org
 * 2007 Dec 09: Format code, use generics more. - dj@opennms.org
 * 2007 Jul 24: Organize imports, eliminate unused code - dj@opennms.org
 * 
 * Created: August 20, 2006
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.outage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
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
            outagerow.put("ipaddr", ipInterface.getIpAddressAsString());
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
