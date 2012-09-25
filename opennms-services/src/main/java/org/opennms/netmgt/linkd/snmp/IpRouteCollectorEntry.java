/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;

public abstract class IpRouteCollectorEntry extends SnmpStore {

    protected IpRouteCollectorEntry(NamedSnmpVar[] list) {
        super(list);
        // TODO Auto-generated constructor stub
    }
    public final static     String  IP_ROUTE_DEST           = "ipRouteDest";
    public final static     String  IP_ROUTE_IFINDEX        = "ipRouteIfIndex";
    public final static     String  IP_ROUTE_METRIC1        = "ipRouteMetric1";
    public final static     String  IP_ROUTE_METRIC2        = "ipRouteMetric2";
    public final static     String  IP_ROUTE_METRIC3        = "ipRouteMetric3";
    public final static     String  IP_ROUTE_METRIC4        = "ipRouteMetric4";
    public final static     String  IP_ROUTE_NXTHOP         = "ipRouteNextHop";
    public final static     String  IP_ROUTE_TYPE           = "ipRouteType";
    public final static     String  IP_ROUTE_PROTO          = "ipRouteProto";
    public final static     String  IP_ROUTE_AGE            = "ipRouteAge";
    public final static     String  IP_ROUTE_MASK           = "ipRouteMask";
    public final static     String  IP_ROUTE_METRIC5        = "ipRouteMetric5";
    public final static     String  IP_ROUTE_INFO           = "ipRouteInfo";
}
