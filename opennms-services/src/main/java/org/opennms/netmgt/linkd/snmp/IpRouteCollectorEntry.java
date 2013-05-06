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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.model.OnmsIpRouteInterface;
import org.opennms.netmgt.model.OnmsIpRouteInterface.RouteType;

public abstract class IpRouteCollectorEntry extends SnmpStore {

    protected IpRouteCollectorEntry(NamedSnmpVar[] list) {
        super(list);
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
    
    public InetAddress getIpRouteDest() {
        return getIPAddress(IP_ROUTE_DEST); 
    }

    public Integer getIpRouteIfIndex() {
        return getInt32(IP_ROUTE_IFINDEX);
    }

    public Integer getIpRouteMetric1() {
        return getInt32(IP_ROUTE_METRIC1);
    }

    public Integer getIpRouteMetric2() {
        return getInt32(IP_ROUTE_METRIC2);

    }

    public Integer getIpRouteMetric3() {
        return getInt32(IP_ROUTE_METRIC3);
    }

    public Integer getIpRouteMetric4() {
        return getInt32(IP_ROUTE_METRIC4);
    }

    public InetAddress getIpRouteNextHop() {
        return getIPAddress(IP_ROUTE_NXTHOP);
    }

    public Integer getIpRouteType() {
        return getInt32(IP_ROUTE_TYPE);
    }

    public Integer getIpRouteProto() {
        return getInt32(IP_ROUTE_PROTO);
    }

    public Integer getIpRouteAge() {
        return getInt32(IP_ROUTE_AGE);
    }

    public InetAddress getIpRouteMask() {
        return getIPAddress(IP_ROUTE_MASK);
    }

    public Integer getIpRouteMetric5() {
        return getInt32(IP_ROUTE_METRIC5);
    }

    public String getIpRouteInfo() {
        return getObjectID(IP_ROUTE_INFO);
    }
    
    public OnmsIpRouteInterface getOnmsIpRouteInterface(OnmsIpRouteInterface ipRouteInterface) {
    	ipRouteInterface.setRouteDest(str(getIpRouteDest()));
        ipRouteInterface.setRouteIfIndex(getIpRouteIfIndex());
        ipRouteInterface.setRouteMask(str(getIpRouteMask()));
        ipRouteInterface.setRouteMetric1(getIpRouteMetric1());
        ipRouteInterface.setRouteMetric2(getIpRouteMetric2());
        ipRouteInterface.setRouteMetric3(getIpRouteMetric3());
        ipRouteInterface.setRouteMetric4(getIpRouteMetric4());
        ipRouteInterface.setRouteMetric5(getIpRouteMetric5());
        ipRouteInterface.setRouteNextHop(str(getIpRouteNextHop()));
        ipRouteInterface.setRouteProto(getIpRouteProto());
        ipRouteInterface.setRouteType(RouteType.get(getIpRouteType()));
        
    	return ipRouteInterface;
    }
}
