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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;


public class FindAllOutages extends OutageMappingQuery {

    public FindAllOutages(DataSource ds) {
        super(ds, "from outages " + "left outer join notifications on (outages.svclosteventid=notifications.eventid), node, ipinterface, ifservices, service " + "where ifregainedservice is null " + "and (node.nodeid=outages.nodeid and ipinterface.ipaddr=outages.ipaddr and ifservices.serviceid=outages.serviceid) " + "and node.nodeType != 'D' and ipinterface.isManaged != 'D' and ifservices.status != 'D' " + "and outages.serviceid=service.serviceid " + " and suppresstime is null or suppresstime < now() " + "order by nodelabel, ipaddr, serviceName");
        
        
        compile();
    }
    
    public FindAllOutages(DataSource ds, Integer offset, Integer limit) {
        super(ds, "from outages " + "left outer join notifications on (outages.svclosteventid=notifications.eventid), node, ipinterface, ifservices, service " + "where ifregainedservice is null " + "and (node.nodeid=outages.nodeid and ipinterface.ipaddr=outages.ipaddr and ifservices.serviceid=outages.serviceid) " + "and node.nodeType != 'D' and ipinterface.isManaged != 'D' and ifservices.status != 'D' " + "and outages.serviceid=service.serviceid " + " (and suppresstime is null or suppresstime < now()) " + "order by nodelabel, ipaddr, serviceName" + " OFFSET " + offset + " LIMIT " + limit);
        
        
        compile();
    }
    
    public FindAllOutages(DataSource ds, Integer offset, Integer limit, String order, String direction) {
        super(ds, "FROM outages as outages, ifservices as ifservices WHERE outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID  and (outages.suppresstime is null or outages.suppresstime < now()) order by  " + order + " " + direction + " LIMIT + " + limit + " OFFSET " + offset  );
        
        
        compile();
    }
    
}