//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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
// Tab Size = 8
//

package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <P>SystemGroup holds the system group properties
 * It implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests /recieve
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class Dot1dStpGroup extends AggregateTracker {
	//
	// Lookup strings for specific table entries
	//
	public final static String STP_PROTOCOL_SPEC = "dot1dStpProtocolSpecification";

	public final static String STP_PRIORITY = "dot1dStpPriority";

	public final static String STP_TIME_LASTTOPCH = "dot1dStpTimeSinceLastTopologyChange";

	public final static String STP_TOP_CHANGES = "dot1dStpTopChanges";

	public final static String STP_DESIGNATED_ROOT = "dot1dStpDesignatedRoot";

	public final static String STP_ROOT_COST = "dot1dStpRootCost";

	public final static String STP_ROOT_PORT = "dot1dStpRootPort";

	public final static String STP_MAX_AGE = "dot1dStpMaxAge";

	public final static String STP_HELLO_TIME = "dot1dStpHelloTime";

	public final static String STP_HOLD_TIME = "dot1dStpHoldTime";

	public final static String STP_FORW_DELAY = "dot1dStpForwardDelay";

	public final static String STP_BRDG_MAX_AGE = "dot1dStpBridgeMaxAge";

	public final static String STP_BRDG_HELLO_TIME = "dot1dStpBridgeHelloTime";

	public final static String STP_BRDG_FORW_DELAY = "dot1dStpBridgeForwardDelay";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the SNMP Interface table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	private static NamedSnmpVar[] ms_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		// Array size 14 elements 
		//
		ms_elemList = new NamedSnmpVar[14];
		int ndx = 0;

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
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PROTOCOL_SPEC, ".1.3.6.1.2.1.17.2.1");

		/**
		 * <P> The value of the write-able portion of the Bridge
		 *  ID, i.e., the first two octets of the (8 octet
		 *  long) Bridge ID. The other (last) 6 octets of the
		 *  Bridge ID are given by the value of
		 *  dot1dBaseBridgeAddress.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PRIORITY, ".1.3.6.1.2.1.17.2.2");

		/**
		 * <P>The time (in hundredths of a second) since the
		 *  last time a topology change was detected by the
		 * bridge entity</P>.
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS,
				STP_TIME_LASTTOPCH, ".1.3.6.1.2.1.17.2.3");

		/**
		 * <P>The total number of topology changes detected by
		 *  this bridge since the management entity was last
		 *  reset or initialized.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
				STP_TOP_CHANGES, ".1.3.6.1.2.1.17.2.4");
	
		/**
		 * <P>The bridge identifier of the root of the spanning
		 *  tree as determined by the Spanning Tree Protocol
		 *  as executed by this node. This value is used as
		 *  the Root Identifier parameter in all Configuration
		 *  Bridge PDUs originated by this node.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				STP_DESIGNATED_ROOT, ".1.3.6.1.2.1.17.2.5");
	
		/**
		 * <P>The cost of the path to the root as seen from
         * this bridge.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_ROOT_COST, ".1.3.6.1.2.1.17.2.6");
	
		/**
		 * <P>The port number of the port which offers the
		 * lowest cost path from this bridge to the root
 		 * bridge.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_ROOT_PORT, ".1.3.6.1.2.1.17.2.7");

		/**
		 * <P>The maximum age of Spanning Tree Protocol
		 * information learned from the network on any port
		 * before it is discarded, in units of hundredths of
		 * a second. This is the actual value that this
		 * bridge is currently using.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_MAX_AGE, ".1.3.6.1.2.1.17.2.8");

		/**
		 * <P>The amount of time between the transmission of
 		 * Configuration bridge PDUs by this node on any port
 		 * when it is the root of the spanning tree or trying
 		 * to become so, in units of hundredths of a second.
 		 * This is the actual value that this bridge is
 		 * currently using.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_HELLO_TIME, ".1.3.6.1.2.1.17.2.9");
		
		/**
		 * <P>This time value determines the interval length
 		 * during which no more than two Configuration bridge
 		 * PDUs shall be transmitted by this node, in units
 		 * of hundredths of a second.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_HOLD_TIME, ".1.3.6.1.2.1.17.2.10");

		/**
		 * <P>This time value, measured in units of hundredths
 		 * of a second, controls how fast a port changes its
 		 * spanning state when moving towards the Forwarding
 		 * state. The value determines how long the port
 		 * stays in each of the Listening and Learning
 		 * states, which precede the Forwarding state. This
 		 * value is also used, when a topology change has
 		 * been detected and is underway, to age all dynamic
 		 * entries in the Forwarding Database. [Note that
 		 * this value is the one that this bridge is
 		 * currently using, in contrast to
 		 * dot1dStpBridgeForwardDelay which is the value that
 		 * this bridge and all others would start using
 		 * if/when this bridge were to become the root.]</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_FORW_DELAY, ".1.3.6.1.2.1.17.2.11");

		/**
		 * <P>The value that all bridges use for MaxAge when
		 *  this bridge is acting as the root. Note that
 		 * 802.1D-1990 specifies that the range for this
 		 * parameter is related to the value of
 		 * dot1dStpBridgeHelloTime. The granularity of this
 		 * timer is specified by 802.1D-1990 to be 1 second.
 		 * An agent may return a badValue error if a set is
 		 * attempted to a value which is not a whole number
 		 * of seconds.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_BRDG_MAX_AGE, ".1.3.6.1.2.1.17.2.12");

		/**
		 * <P>The value that all bridges use for HelloTime when
 		 * this bridge is acting as the root. The
 		 * granularity of this timer is specified by 802.1D-
 		 * 1990 to be 1 second. An agent may return a
 		 * badValue error if a set is attempted to a value
 		 * which is not a whole number of seconds.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_BRDG_HELLO_TIME, ".1.3.6.1.2.1.17.2.13");
		
		/**
		 * <P>The value that all bridges use for ForwardDelay
		 *  when this bridge is acting as the root. Note that
 		 * 802.1D-1990 specifies that the range for this
 		 * parameter is related to the value of
 		 * dot1dStpBridgeMaxAge. The granularity of this
 		 * timer is specified by 802.1D-1990 to be 1 second.
 		 * An agent may return a badValue error if a set is
 		 * attempted to a value which is not a whole number
 		 * of seconds.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_BRDG_FORW_DELAY, ".1.3.6.1.2.1.17.2.14");
		

	}

	/**
	 * <P>The SYSTEM_OID is the object identifier that represents the
	 * root of the system information in the MIB forest. Each of the
	 * system elements can be retreived by adding their specific index
	 * to the string, and an additional Zero(0) to signify the single 
	 * instance item.</P>
	 */
	public static final String SYSTEM_OID = ".1.3.6.1.2.1.17.2";

    private SnmpStore m_store;
    private InetAddress m_address;

	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
	 * @param session	The SNMP session with the remote agent.
	 * @param signaler	The object signaled when data collection is done.
	 *
	 */
	public Dot1dStpGroup(InetAddress address) {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_address = address;
        m_store = new SnmpStore(ms_elemList); 
    }
    
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    protected void reportGenErr(String msg) {
        log().warn("Error retrieving systemGroup from "+m_address+". "+msg);
    }

    protected void reportNoSuchNameErr(String msg) {
        log().info("Error retrieving systemGroup from "+m_address+". "+msg);
    }

    private final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public int getStpProtocolSpecification(){
    	Integer stpProtocolSpecification = m_store.getInt32(STP_PROTOCOL_SPEC);
    	if (stpProtocolSpecification == null) {
            return -1;
        }
    	return stpProtocolSpecification;
    }
	
    public int getStpPriority(){
    	Integer stpPriority = m_store.getInt32(STP_PRIORITY);
    	if (stpPriority == null ) {
            return -1;
        }
    	return stpPriority;
    }

    public SnmpValue getStpTimeSinceLastTopologyChange(){
    	return m_store.getValue(STP_TIME_LASTTOPCH);
    }

    public int getStpTopologyChanges(){
    	return m_store.getInt32(STP_TOP_CHANGES);
    }

    public String getStpDesignatedRoot(){
    	return m_store.getHexString(STP_DESIGNATED_ROOT);
    }
    
    public int getStpRootCost(){
    	Integer stpRootCost = m_store.getInt32(STP_ROOT_COST); 
    	if (stpRootCost == null) {
            return -1;
        }
    	return stpRootCost;
    }

    public int getStpRootPort(){
    	Integer stpRootPort = m_store.getInt32(STP_ROOT_PORT);
    	if (stpRootPort == null) {
            return -1;
        }
    	return stpRootPort;
    }

    public int getStpMaxAge(){
    	Integer stpMaxAge = m_store.getInt32(STP_MAX_AGE);
    	if (stpMaxAge == null ) {
            return -1;
        }
    	return stpMaxAge;
    }

    public int getStpHelloTime(){
    	Integer stpHelloTime = m_store.getInt32(STP_HELLO_TIME); 
    	if (stpHelloTime == null) {
            return -1;
        }
    	return stpHelloTime;
    }

    public int getStpHoldTime(){
    	Integer stpHoldTime = m_store.getInt32(STP_HOLD_TIME); 
    	if (stpHoldTime == null) {
            return -1;
        }
    	return stpHoldTime;
    }

    public int getStpForwardDelay(){
 
    	Integer stpForwardDelay = m_store.getInt32(STP_FORW_DELAY);
    	if (stpForwardDelay == null) {
            return -1;
        }
    	return stpForwardDelay;
    }

    public int getStpBridgeMaxAge(){
    	Integer stpBridgeMaxAge = m_store.getInt32(STP_BRDG_MAX_AGE);
    	if (stpBridgeMaxAge == null ) {
            return -1;
        }
    	return stpBridgeMaxAge;
    }

    public int getStpBridgeHelloTime(){
    	Integer stpBridgeHelloTime = m_store.getInt32(STP_BRDG_HELLO_TIME);
    	if (stpBridgeHelloTime == null ) {
            return -1;
        }
    	return stpBridgeHelloTime;
    }

    public int getStpBridgeForwardDelay(){
    	Integer stpBridgeForwardDelay = m_store.getInt32(STP_BRDG_FORW_DELAY);
    	if (stpBridgeForwardDelay == null ) {
            return -1;
        }
    	return stpBridgeForwardDelay;
    }

}