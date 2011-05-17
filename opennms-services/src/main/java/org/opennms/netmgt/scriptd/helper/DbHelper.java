package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.utils.SingleResultQuerier;

public class DbHelper {

	private static String GETNODELABELSTRING = "select nodeLabel from node where nodeId = ?";
	
	public static String getNodeLabel(Integer nodeid) {
		SingleResultQuerier querier = new SingleResultQuerier(DataSourceFactory.getInstance(), GETNODELABELSTRING);
		querier.execute(nodeid);
		return (String) querier.getResult();
	}
}
