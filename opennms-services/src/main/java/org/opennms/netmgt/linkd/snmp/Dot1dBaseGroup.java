/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;

import org.opennms.netmgt.model.OnmsStpNode;
import org.opennms.netmgt.model.OnmsStpNode.BridgeBaseType;

import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;

/**
 * <P>Dot1dBaseGroup holds the dot1dBridge.dot1dBase group properties
 * It implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests/receive
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@opennms.org">Antonio Russo</A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1dBaseGroup extends AggregateTracker
{
	
    /**
     * the bridge type
     */
	//
	// Lookup strings for specific table entries
	//
	/** Constant <code>BASE_BRIDGE_ADDRESS="dot1dBaseBridgeAddress"</code> */
	public final static	String	BASE_BRIDGE_ADDRESS	= "dot1dBaseBridgeAddress";
	/** Constant <code>BASE_NUM_PORTS="dot1dBaseNumPorts"</code> */
	public final static	String	BASE_NUM_PORTS		= "dot1dBaseNumPorts";
	/** Constant <code>BASE_NUM_TYPE="dot1dBaseType"</code> */
	public final static	String	BASE_NUM_TYPE		= "dot1dBaseType";
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the STP Node table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public final static NamedSnmpVar[] ms_elemList = new NamedSnmpVar[] {
		/**
		 * <P>The MAC address used by this bridge when it must
		 * be referred to in a unique fashion. It is
		 * recommended that this be the numerically smallest
		 *  MAC address of all ports that belong to this
		 *  bridge. However it is only required to be unique.
		 *  When concatenated with dot1dStpPriority a unique
		 *  BridgeIdentifier is formed which is used in the
		 *  Spanning Tree Protocol.</P>
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,BASE_BRIDGE_ADDRESS,".1.3.6.1.2.1.17.1.1"),

		/**
		 * <P> The number of ports controlled by this bridging entity.</P>
		 * 
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32,BASE_NUM_PORTS,".1.3.6.1.2.1.17.1.2"),

		/**
		 * <P> Indicates what type of bridging this bridge can
		 *  perform. If a bridge is actually performing a
		 *  certain type of bridging this will be indicated by
		 *  entries in the port table for the given type.</P>
		 *  values:
		 *  1 = unknown
		 *  2 = transparent-only
		 *  3 = sourceroute-only
		 *  4 = srt
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32,BASE_NUM_TYPE,".1.3.6.1.2.1.17.1.3")
	};

	/**
	 * <P>The SYSTEM_OID is the object identifier that represents the
	 * root of the system information in the MIB forest. Each of the
	 * system elements can be retrieved by adding their specific index
	 * to the string, and an additional Zero(0) to signify the single 
	 * instance item.</P>
	 */
	public static final String	SYSTEM_OID 	= ".1.3.6.1.2.1.17.1";

    private SnmpStore m_store;
    private InetAddress m_address;
	
	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 */
	public Dot1dBaseGroup(InetAddress address) {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_address = address;
        m_store = new SnmpStore(ms_elemList); 
	}

    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(final String msg) {
        LogUtils.warnf(this, "Error retrieving systemGroup from %s: %s", m_address, msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(final String msg) {
        LogUtils.infof(this, "Error retrieving systemGroup from %s: %s", m_address, msg);
    }

    /**
     * <p>getBridgeAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBridgeAddress() {
        return m_store.getHexString(BASE_BRIDGE_ADDRESS);
    }
    
    /**
     * <p>getNumberOfPorts</p>
     *
     * @return a int.
     */
    public Integer getNumberOfPorts() {
    	return m_store.getInt32(BASE_NUM_PORTS);
    }

    /**
     * <p>getBridgeType</p>
     *
     * @return a int.
     */
    public Integer getBridgeType() {
    	return m_store.getInt32(BASE_NUM_TYPE);
    }
    
    public OnmsStpNode getOnmsStpNode(OnmsStpNode node) {
    	if (getBridgeAddress() == null) 
    		return node;
    	node.setBaseBridgeAddress(getBridgeAddress());
    	if (getBridgeType() == null)
    		node.setBaseType(BridgeBaseType.UNKNOWN);
    	else
    		node.setBaseType(BridgeBaseType.get(getBridgeType()));
    	node.setBaseNumPorts(getNumberOfPorts());
    	return node;
    }
}
