/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dBaseType;
import org.opennms.netmgt.enlinkd.model.BridgeElement.BridgeDot1dStpProtocolSpecification;
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
public class Dot1dBaseTracker extends AggregateTracker
{
	private final static Logger LOG = LoggerFactory.getLogger(Dot1dBaseTracker.class);

    /*
     * the bridge type
     */
	/** Constant <code>BASE_BRIDGE_ADDRESS="dot1dBaseBridgeAddress"</code> */
	public final static	String	BASE_BRIDGE_ADDRESS	= "dot1dBaseBridgeAddress";
	public final static	String	BASE_BRIDGE_ADDRESS_OID	= ".1.3.6.1.2.1.17.1.1";
	/** Constant <code>BASE_NUM_PORTS="dot1dBaseNumPorts"</code> */
	public final static	String	BASE_NUM_PORTS		= "dot1dBaseNumPorts";
	public final static	String	BASE_NUM_PORTS_OID	= ".1.3.6.1.2.1.17.1.2";
	/** Constant <code>BASE_NUM_TYPE="dot1dBaseType"</code> */
	public final static	String	BASE_NUM_TYPE		= "dot1dBaseType";
	public final static	String	BASE_NUM_TYPE_OID	= ".1.3.6.1.2.1.17.1.3";
	/** Constant <code>STP_PROTOCOL_SPEC="dot1dStpProtocolSpecification"</code> */
	public final static String STP_PROTOCOL_SPEC = "dot1dStpProtocolSpecification";
	public final static String STP_PROTOCOL_SPEC_OID = ".1.3.6.1.2.1.17.2.1";
	/** Constant <code>STP_PRIORITY="dot1dStpPriority"</code> */
	public final static String STP_PRIORITY = "dot1dStpPriority";
	public final static String STP_PRIORITY_OID = ".1.3.6.1.2.1.17.2.2";
	/** Constant <code>STP_DESIGNATED_ROOT="dot1dStpDesignatedRoot"</code> */
	public final static String STP_DESIGNATED_ROOT = "dot1dStpDesignatedRoot";
	public final static String STP_DESIGNATED_ROOT_OID =  ".1.3.6.1.2.1.17.2.5";
	/** Constant <code>STP_ROOT_COST="dot1dStpRootCost"</code> */
	public final static String STP_ROOT_COST = "dot1dStpRootCost";
	public final static String STP_ROOT_COST_OID = ".1.3.6.1.2.1.17.2.6";
	/** Constant <code>STP_ROOT_PORT="dot1dStpRootPort"</code> */
	public final static String STP_ROOT_PORT = "dot1dStpRootPort";
	public final static String STP_ROOT_PORT_OID = ".1.3.6.1.2.1.17.2.7";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the STP Node table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] ms_elemList;

	static {
		ms_elemList = new NamedSnmpVar[8];
		int ndx = 0;
		/*
		 * <P>The MAC address used by this bridge when it must
		 * be referred to in a unique fashion. It is
		 * recommended that this be the numerically smallest
		 *  MAC address of all ports that belong to this
		 *  bridge. However it is only required to be unique.
		 *  When concatenated with dot1dStpPriority a unique
		 *  BridgeIdentifier is formed which is used in the
		 *  Spanning Tree Protocol.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, BASE_BRIDGE_ADDRESS, BASE_BRIDGE_ADDRESS_OID);

		/*
		 * <P> The number of ports controlled by this bridging entity.</P>
		 *
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, BASE_NUM_PORTS, BASE_NUM_PORTS_OID);

		/*
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
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, BASE_NUM_TYPE, BASE_NUM_TYPE_OID);

		/*
		 * <P>An indication of what version of the Spanning
		 *  Tree Protocol is being run. The value
		 *  'decLb100(2)' indicates the DEC LANbridge 100
		 *  Spanning Tree protocol. IEEE 802.1d
		 *  implementations will return 'ieee8021d(3)'. If
		 *  future versions of the IEEE Spanning Tree Protocol
		 *  are released that are incompatible with the
		 *  current version a new value will be defined.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_PROTOCOL_SPEC, STP_PROTOCOL_SPEC_OID);

		/*
		 * <P> The value of the write-able portion of the Bridge
		 *  ID, i.e., the first two octets of the (8 octet
		 *  long) Bridge ID. The other (last) 6 octets of the
		 *  Bridge ID are given by the value of
		 *  dot1dBaseBridgeAddress.</P>
		 *
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_PRIORITY, STP_PRIORITY_OID);

		/*
		 * <P>The bridge identifier of the root of the spanning
		 *  tree as determined by the Spanning Tree Protocol
		 *  as executed by this node. This value is used as
		 *  the Root Identifier parameter in all Configuration
		 *  Bridge PDUs originated by this node.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, STP_DESIGNATED_ROOT, STP_DESIGNATED_ROOT_OID);

		/*
		 * <P>The cost of the path to the root as seen from
		 * this bridge.</P>
		 *
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_ROOT_COST, STP_ROOT_COST_OID);

		/*
		 * <P>The port number of the port which offers the
		 * lowest cost path from this bridge to the root
		 * bridge.</P>
		 *
		 */
		ms_elemList[ndx] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, STP_ROOT_PORT, STP_ROOT_PORT_OID);
	}

    private final SnmpStore m_store;
	
	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
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

	@Override
	public void printSnmpData() {
		System.out.printf("\t\t%s (%s)= %s\n", BASE_BRIDGE_ADDRESS_OID, BASE_BRIDGE_ADDRESS, getBridgeAddress());
		System.out.printf("\t\t%s (%s)= %s\n", BASE_NUM_PORTS_OID, BASE_NUM_PORTS, getNumberOfPorts());
		System.out.printf("\t\t%s (%s)= %s (%s)\n", BASE_NUM_TYPE_OID, BASE_NUM_TYPE, getBridgeType(), BridgeDot1dBaseType.getTypeString(getBridgeType()));
		System.out.printf("\t\t%s (%s)= %s (%s)\n", STP_PROTOCOL_SPEC_OID, STP_PROTOCOL_SPEC, getStpProtocolSpecification(), BridgeDot1dStpProtocolSpecification.getTypeString(getStpProtocolSpecification()));
		System.out.printf("\t\t%s (%s)= %s\n", STP_PRIORITY_OID, STP_PRIORITY, getStpPriority());
		System.out.printf("\t\t%s (%s)= %s\n", STP_DESIGNATED_ROOT_OID, STP_DESIGNATED_ROOT, getStpDesignatedRoot());
		System.out.printf("\t\t%s (%s)= %s\n", STP_ROOT_COST_OID, STP_ROOT_COST, getStpRootCost());
		System.out.printf("\t\t%s (%s)= %s\n", STP_ROOT_PORT_OID, STP_ROOT_PORT, getStpRootPort());

	}
    
}
