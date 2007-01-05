//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.web.svclayer.outage;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

public class OutageListBuilder {

	public List theTable(Collection<OnmsOutage> foundOutages) {

		List theTable = new LinkedList();
		Locale locale = Locale.getDefault();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",locale);
		

		for (Iterator iter = foundOutages.iterator(); iter.hasNext();) {
			OnmsOutage outage = (OnmsOutage) iter.next();
			OnmsMonitoredService monitoredService = outage
					.getMonitoredService();
			OnmsServiceType serviceType = monitoredService.getServiceType();
			OnmsIpInterface ipInterface = monitoredService.getIpInterface();
			
			// ips.put(outage.getId(), ipInterface.getIpAddress());
			// nodes.put(outage.getId(), ipInterface.getNode().getLabel());
			// nodeids.put(outage.getId(), monitoredService.getNodeId());
			// services.put(outage.getId(), serviceType.getName());
			
			

			Map outagerow = new HashMap();
			outagerow.put("outageid", (String) outage.getId().toString());
			outagerow.put("node", ipInterface.getNode().getLabel());
			outagerow.put("nodeid", (String) monitoredService.getNodeId()
					.toString());
			outagerow.put("ipaddr", ipInterface.getIpAddress());
			outagerow.put("interfaceid", ipInterface.getId());
			outagerow.put("service", serviceType.getName());
			outagerow.put("serviceid", serviceType.getId());
			outagerow.put("eventid", outage.getServiceLostEvent().getId());

			
			
			// if (outage.getIfLostService() != null) {
			// outagerow.put("down",
			// formatter.format(outage.getIfLostService()));
			// }

			if (outage.getIfLostService() != null) {
				outagerow
						.put("iflostservice", formatter.format( outage.getIfLostService()));
						
						// Long format for searches
				
						outagerow.put("iflostservicelong",(Long)outage.getIfLostService().getTime());
				
						// Format the date-output.
				
			}

			if (outage.getIfRegainedService() != null) {
				outagerow.put("ifregainedservice", formatter.format( outage
						.getIfRegainedService()));
				
				outagerow.put("ifregainedservicelong", (Long)outage.getIfRegainedService().getTime());
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
