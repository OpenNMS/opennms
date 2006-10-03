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
	
	int nextHopnodeid;
	
	int nextHopIfindex;
	
	InetAddress routedest;

	InetAddress routemask;

	InetAddress nextHop;

	InetAddress nextHopNetmask;
	
	int snmpiftype; 
	
	RouterInterface(int nextHopnodeid, int nextHopIfindex, String nextHopNetmask) {
		this.nextHopnodeid = nextHopnodeid;
		this.nextHopIfindex = nextHopIfindex;
		try {
			this.nextHopNetmask = InetAddress.getByName(nextHopNetmask);
		} catch (Exception e) {
			
		}
	}

	RouterInterface(int nextHopnodeid, int nextHopIfindex) {
		this.nextHopnodeid = nextHopnodeid;
		this.nextHopIfindex = nextHopIfindex;
		try {
			this.nextHopNetmask = InetAddress.getByName("255.255.255.255");
		} catch (Exception e) {
			
		}
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
	
	public InetAddress getNetmask() {
		return nextHopNetmask;
	}
	public void setNetmask(InetAddress netmask) {
		this.nextHopNetmask = netmask;
	}
	public int getNextHopNodeid() {
		return nextHopnodeid;
	}
	public void setNextHopNodeid(int nexhopnodeid) {
		this.nextHopnodeid = nexhopnodeid;
	}
	public int getNextHopIfindex() {
		return nextHopIfindex;
	}
	public void setNextHopIfindex(int nextHopIfindex) {
		this.nextHopIfindex = nextHopIfindex;
	}

	public void setIfindex(int ifindex) {
		this.ifindex = ifindex;
	}
	
	public InetAddress getNextHopNet() {
		byte[] ipAddress = nextHop.getAddress();
		byte[] netMask = nextHopNetmask.getAddress();
		byte[] netWork = new byte[4];

		for (int i=0;i< 4; i++) {
			netWork[i] = new Integer(ipAddress[i] & netMask[i]).byteValue();
			
		}
		try {
			return InetAddress.getByAddress(netWork);
		} catch (Exception e){
			return null;
		}
	}

	public InetAddress getRouteNet() {
		byte[] ipAddress = routedest.getAddress();
		byte[] netMask = routemask.getAddress();
		byte[] netWork = new byte[4];

		for (int i=0;i< 4; i++) {
			netWork[i] = new Integer(ipAddress[i] & netMask[i]).byteValue();
			
		}
		try {
			return InetAddress.getByAddress(netWork);
		} catch (Exception e){
			return null;
		}
	}

	public InetAddress getRouteDest() {
		return routedest;
	}

	public void setRouteDest(InetAddress routedest) {
		this.routedest = routedest;
	}

	public InetAddress getRoutemask() {
		return routemask;
	}

	public void setRoutemask(InetAddress routemask) {
		this.routemask = routemask;
	}
}

