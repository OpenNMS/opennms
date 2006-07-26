/*
 * Created on 9-mar-2006
 *
 * Class that holds informations for node bridges
 */
package org.opennms.netmgt.linkd;

import java.net.InetAddress;
/**
 * @author antonio
 *
 */

public class RouterInterface {
	
	int ifindex;
	
	int metric;
	
	InetAddress nextHop;
	
	int snmpiftype; 
	
	/**
	 * @return Returns the nodeparentid.
	 */
	public int getNodeparentid() {
		return nodeparentid;
	}
	/**
	 * @param nodeparentid The nodeparentid to set.
	 */
	public void setNodeparentid(int nodeparentid) {
		this.nodeparentid = nodeparentid;
	}
	int nodeparentid;

	RouterInterface(int ifindex) {
		this.ifindex = ifindex;
	}

	/**
	 * @return Returns the ifindex.
	 */
	public int getIfindex() {
		return ifindex;
	}
	/**
	 * @return Returns the metric.
	 */
	public int getMetric() {
		return metric;
	}
	/**
	 * @param metric The metric to set.
	 */
	public void setMetric(int metric) {
		this.metric = metric;
	}
	/**
	 * @return Returns the nextHop.
	 */
	public InetAddress getNextHop() {
		return nextHop;
	}
	/**
	 * @param nextHop The nextHop to set.
	 */
	public void setNextHop(InetAddress nextHop) {
		this.nextHop = nextHop;
	}
	/**
	 * @return Returns the snmpiftype.
	 */
	public int getSnmpiftype() {
		return snmpiftype;
	}
	/**
	 * @param snmpiftype The snmpiftype to set.
	 */
	public void setSnmpiftype(int snmpiftype) {
		this.snmpiftype = snmpiftype;
	}
}
