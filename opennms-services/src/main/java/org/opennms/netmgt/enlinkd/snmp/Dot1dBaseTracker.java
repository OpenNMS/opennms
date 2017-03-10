/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.model.BridgeElement;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>Dot1dBase holds the dot1dBridge.dot1dBase group properties
 * It implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests/receive
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@opennms.org">Antonio Russo</A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1dBaseTracker extends AggregateTracker
{
	private final static Logger LOG = LoggerFactory.getLogger(Dot1dBaseTracker.class);
    /**
     * the bridge type
     */
	/** Constant <code>BASE_BRIDGE_ADDRESS="dot1dBaseBridgeAddress"</code> */
	public final static	String	BASE_BRIDGE_ADDRESS	= "dot1dBaseBridgeAddress";
	/** Constant <code>BASE_NUM_PORTS="dot1dBaseNumPorts"</code> */
	public final static	String	BASE_NUM_PORTS		= "dot1dBaseNumPorts";
	/** Constant <code>BASE_NUM_TYPE="dot1dBaseType"</code> */
	public final static	String	BASE_NUM_TYPE		= "dot1dBaseType";
	/** Constant <code>STP_PROTOCOL_SPEC="dot1dStpProtocolSpecification"</code> */
	public final static String STP_PROTOCOL_SPEC = "dot1dStpProtocolSpecification";
	/** Constant <code>STP_PRIORITY="dot1dStpPriority"</code> */
	public final static String STP_PRIORITY = "dot1dStpPriority";
	/** Constant <code>STP_DESIGNATED_ROOT="dot1dStpDesignatedRoot"</code> */
	public final static String STP_DESIGNATED_ROOT = "dot1dStpDesignatedRoot";
	/** Constant <code>STP_ROOT_COST="dot1dStpRootCost"</code> */
	public final static String STP_ROOT_COST = "dot1dStpRootCost";
	/** Constant <code>STP_ROOT_PORT="dot1dStpRootPort"</code> */
	public final static String STP_ROOT_PORT = "dot1dStpRootPort";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the STP Node table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] ms_elemList = null;
	
	static {
		ms_elemList = new NamedSnmpVar[8]; 
		int ndx = 0;
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
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,BASE_BRIDGE_ADDRESS,".1.3.6.1.2.1.17.1.1");

		/**
		 * <P> The number of ports controlled by this bridging entity.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,BASE_NUM_PORTS,".1.3.6.1.2.1.17.1.2");

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
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,BASE_NUM_TYPE,".1.3.6.1.2.1.17.1.3");

		/**
		 * <P>An indication of what version of the Spanning
		 *  Tree Protocol is being run. The value
		 *  'decLb100(2)' indicates the DEC LANbridge 100
		 *  Spanning Tree protocol. IEEE 802.1d
		 *  implementations will return 'ieee8021d(3)'. If
		 *  future versions of the IEEE Spanning Tree Protocol
		 *  are released that are incompatible with the
		 *  current version a new value will be defined.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_PROTOCOL_SPEC, ".1.3.6.1.2.1.17.2.1");

		/**
		 * <P> The value of the write-able portion of the Bridge
		 *  ID, i.e., the first two octets of the (8 octet
		 *  long) Bridge ID. The other (last) 6 octets of the
		 *  Bridge ID are given by the value of
		 *  dot1dBaseBridgeAddress.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_PRIORITY, ".1.3.6.1.2.1.17.2.2");

		/**
		 * <P>The bridge identifier of the root of the spanning
		 *  tree as determined by the Spanning Tree Protocol
		 *  as executed by this node. This value is used as
		 *  the Root Identifier parameter in all Configuration
		 *  Bridge PDUs originated by this node.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, STP_DESIGNATED_ROOT, ".1.3.6.1.2.1.17.2.5");

		/**
		 * <P>The cost of the path to the root as seen from
         * this bridge.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_ROOT_COST, ".1.3.6.1.2.1.17.2.6");
	
		/**
		 * <P>The port number of the port which offers the
		 * lowest cost path from this bridge to the root
 		 * bridge.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_ROOT_PORT, ".1.3.6.1.2.1.17.2.7");
	}

    private SnmpStore m_store;
	
	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 */
	public Dot1dBaseTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList); 
	}

    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(final String msg) {
        LOG.warn("Error retrieving dot1dbase: {}", msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(final String msg) {
        LOG.info("Error retrieving dot1dbase: {}", msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving dot1dbase: {}", ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) retrieving dot1dbase: {}", status, status.retry()? "Retrying." : "Giving up.");
    }

    /**
     * <p>getBridgeAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBridgeAddress() {
    	if (m_store.getValue(BASE_BRIDGE_ADDRESS) != null)
    		return m_store.getHexString(BASE_BRIDGE_ADDRESS);
    	return null;
    }
    
    /**
     * <p>getNumberOfPorts</p>
     *
     * @return a int.
     */
    public Integer getNumberOfPorts() {
    	if (m_store.getValue(BASE_NUM_PORTS) != null)
    		return m_store.getInt32(BASE_NUM_PORTS);
    	return null;
    }

    /**
     * <p>getBridgeType</p>
     *
     * @return a int.
     */
    public Integer getBridgeType() {
    	if (m_store.getValue(BASE_NUM_TYPE) != null)
    		return m_store.getInt32(BASE_NUM_TYPE);
    	return null;
    }
    
    public Integer getStpProtocolSpecification(){
    	if (m_store.getValue(STP_PROTOCOL_SPEC) != null)
    	return m_store.getInt32(STP_PROTOCOL_SPEC);
    	return null;
    }
	
    public Integer getStpPriority(){
    	if (m_store.getValue(STP_PRIORITY) != null)
    		return m_store.getInt32(STP_PRIORITY);
    	return null;
    }
    
    /**
     * <p>getStpDesignatedRoot</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStpDesignatedRoot(){
    	if (m_store.getValue(STP_DESIGNATED_ROOT) != null)
    		return m_store.getHexString(STP_DESIGNATED_ROOT);
    	return null;
    }

    /**
     * <p>getStpRootCost</p>
     *
     * @return a int.
     */
    public Integer getStpRootCost(){
    	if (m_store.getValue(STP_ROOT_COST) != null)
    		return m_store.getInt32(STP_ROOT_COST); 
    	return null;
    }

    /**
     * <p>getStpRootPort</p>
     *
     * @return a int.
     */
    public Integer getStpRootPort(){
    	if (m_store.getValue(STP_ROOT_PORT) != null)
    		return m_store.getInt32(STP_ROOT_PORT);
    	return null;
    }

    public BridgeElement getBridgeElement() {
    	BridgeElement bridge = new BridgeElement();
    	bridge.setBaseBridgeAddress(getBridgeAddress());
    	bridge.setBaseNumPorts(getNumberOfPorts());
    	if (getBridgeType() != null)
    		bridge.setBaseType(BridgeDot1dBaseType.get(getBridgeType()));
    	if (getStpProtocolSpecification() != null) {
    		bridge.setStpProtocolSpecification(BridgeDot1dStpProtocolSpecification.get(getStpProtocolSpecification()));
    		bridge.setStpPriority(getStpPriority());
    		bridge.setStpDesignatedRoot(getStpDesignatedRoot());
    		bridge.setStpRootPort(getStpRootPort());
    		bridge.setStpRootCost(getStpRootCost());
    	}
    	return bridge;
    }
    
}
