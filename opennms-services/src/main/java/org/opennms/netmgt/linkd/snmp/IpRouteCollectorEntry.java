package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;

public abstract class IpRouteCollectorEntry extends SnmpTableEntry {

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
