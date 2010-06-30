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
/*
 * Created on 9-mar-2006
 *
 * Class that holds informations for node bridges
 */
package org.opennms.netmgt.linkd;

import java.net.InetAddress;
/**
 * <p>RouterInterface class.</p>
 *
 * @author antonio
 * @version $Id: $
 */
public class RouterInterface {
	
	int ifindex;
	
	int metric;
	
	InetAddress routedest;

	InetAddress routemask;

	InetAddress nextHop;

	int nextHopnodeid;
	
	int nextHopIfindex;
	
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
	 * <p>Getter for the field <code>ifindex</code>.</p>
	 *
	 * @return Returns the ifindex.
	 */
	public int getIfindex() {
		return ifindex;
	}
	/**
	 * <p>Getter for the field <code>metric</code>.</p>
	 *
	 * @return Returns the metric.
	 */
	public int getMetric() {
		return metric;
	}
	/**
	 * <p>Setter for the field <code>metric</code>.</p>
	 *
	 * @param metric The metric to set.
	 */
	public void setMetric(int metric) {
		this.metric = metric;
	}
	/**
	 * <p>Getter for the field <code>nextHop</code>.</p>
	 *
	 * @return Returns the nextHop.
	 */
	public InetAddress getNextHop() {
		return nextHop;
	}
	/**
	 * <p>Setter for the field <code>nextHop</code>.</p>
	 *
	 * @param nextHop The nextHop to set.
	 */
	public void setNextHop(InetAddress nextHop) {
		this.nextHop = nextHop;
	}
	/**
	 * <p>Getter for the field <code>snmpiftype</code>.</p>
	 *
	 * @return Returns the snmpiftype.
	 */
	public int getSnmpiftype() {
		return snmpiftype;
	} 
	
	/**
	 * <p>Setter for the field <code>snmpiftype</code>.</p>
	 *
	 * @param snmpiftype The snmpiftype to set.
	 */
	public void setSnmpiftype(int snmpiftype) {
		this.snmpiftype = snmpiftype;
	}
	
	/**
	 * <p>getNetmask</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getNetmask() {
		return nextHopNetmask;
	}
	/**
	 * <p>setNetmask</p>
	 *
	 * @param netmask a {@link java.net.InetAddress} object.
	 */
	public void setNetmask(InetAddress netmask) {
		this.nextHopNetmask = netmask;
	}
	/**
	 * <p>getNextHopNodeid</p>
	 *
	 * @return a int.
	 */
	public int getNextHopNodeid() {
		return nextHopnodeid;
	}
	/**
	 * <p>setNextHopNodeid</p>
	 *
	 * @param nexhopnodeid a int.
	 */
	public void setNextHopNodeid(int nexhopnodeid) {
		this.nextHopnodeid = nexhopnodeid;
	}
	/**
	 * <p>Getter for the field <code>nextHopIfindex</code>.</p>
	 *
	 * @return a int.
	 */
	public int getNextHopIfindex() {
		return nextHopIfindex;
	}
	/**
	 * <p>Setter for the field <code>nextHopIfindex</code>.</p>
	 *
	 * @param nextHopIfindex a int.
	 */
	public void setNextHopIfindex(int nextHopIfindex) {
		this.nextHopIfindex = nextHopIfindex;
	}

	/**
	 * <p>Setter for the field <code>ifindex</code>.</p>
	 *
	 * @param ifindex a int.
	 */
	public void setIfindex(int ifindex) {
		this.ifindex = ifindex;
	}
	
	/**
	 * <p>getNextHopNet</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
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

	/**
	 * <p>getRouteNet</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
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

	/**
	 * <p>getRouteDest</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getRouteDest() {
		return routedest;
	}

	/**
	 * <p>setRouteDest</p>
	 *
	 * @param routedest a {@link java.net.InetAddress} object.
	 */
	public void setRouteDest(InetAddress routedest) {
		this.routedest = routedest;
	}

	/**
	 * <p>Getter for the field <code>routemask</code>.</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getRoutemask() {
		return routemask;
	}

	/**
	 * <p>Setter for the field <code>routemask</code>.</p>
	 *
	 * @param routemask a {@link java.net.InetAddress} object.
	 */
	public void setRoutemask(InetAddress routemask) {
		this.routemask = routemask;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		String stringa = "";
		stringa += "routedest = " + routedest + "\n"; 
		stringa += "routemask = " + routemask + "\n";
		stringa += "routeifindex = " + ifindex + "\n";
		stringa += "routemetric = " + metric + "\n";
		stringa += "nexthop = " + nextHop + "\n";
		stringa += "nexthopmask = " + nextHopNetmask + "\n";
		stringa += "nexthopnodeid = " + nextHopnodeid + "\n";
		stringa += "nexthopifindex = " + nextHopIfindex + "\n";
		stringa += "snmpiftype = " + snmpiftype + "\n";
		stringa += "routenet = " + getRouteNet() + "\n";
		stringa += "nexthopnet = " + getNextHopNet() + "\n";

		
		return stringa;
	}
}

