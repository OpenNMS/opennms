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

}
