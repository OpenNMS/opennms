package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;

public class FindSuppressedOutages  extends OutageMappingQuery {

    public FindSuppressedOutages(DataSource ds) {
        super(ds,"from outages " + "left outer join notifications on (outages.svclosteventid=notifications.eventid), node, ipinterface, ifservices, service " + "where ifregainedservice is null " + "and (node.nodeid=outages.nodeid and ipinterface.ipaddr=outages.ipaddr and ifservices.serviceid=outages.serviceid) " + "and node.nodeType != 'D' and ipinterface.isManaged != 'D' and ifservices.status != 'D' " + "and outages.serviceid=service.serviceid " + " and suppresstime > now() " + "order by nodelabel, outages.ipaddr, serviceName");
        compile();
    }
    
    public FindSuppressedOutages(DataSource ds, Integer offset, Integer limit) {
        super(ds,"from outages " + "left outer join notifications on (outages.svclosteventid=notifications.eventid), node, ipinterface, ifservices, service " + "where ifregainedservice is null " + "and (node.nodeid=outages.nodeid and ipinterface.ipaddr=outages.ipaddr and ifservices.serviceid=outages.serviceid) " + "and node.nodeType != 'D' and ipinterface.isManaged != 'D' and ifservices.status != 'D' " + "and outages.serviceid=service.serviceid " + " and suppresstime > now() " + "order by nodelabel, outages.ipaddr, serviceName" + " OFFSET " + offset + " LIMIT " + limit);
        compile();
    }

	public FindSuppressedOutages(DataSource ds, Integer offset, Integer limit, String order, String direction) {
		super(ds,"FROM outages as outages, ifservices as ifservices WHERE outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID  and (outages.ifRegainedService is null and outages.suppresstime > now())  order by  " + order + " " + direction + " LIMIT + " + limit + " OFFSET " + offset);
        compile();
        }

}
