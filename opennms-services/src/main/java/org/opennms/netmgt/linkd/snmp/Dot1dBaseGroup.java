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

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;

/**
 * <P>Dot1dBaseGroup holds the dot1dBridge.dot1dBase group properties
 * It implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests /recieve
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@opennms.org">Antonio Russo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:rssntn67@opennms.org">Antonio Russo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:rssntn67@opennms.org">Antonio Russo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:rssntn67@opennms.org">Antonio Russo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1dBaseGroup extends AggregateTracker
{
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
	public static NamedSnmpVar[]	ms_elemList = null;
	
	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static
	{
		// Array size is 3.
		//
		ms_elemList = new NamedSnmpVar[3]; 
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
		 *  2 = trasparent-only
		 *  3 = sourceroute-only
		 *  4 = srt
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,BASE_NUM_TYPE,".1.3.6.1.2.1.17.1.3");
	}

	/**
	 * <P>The SYSTEM_OID is the object identifier that represents the
	 * root of the system information in the MIB forest. Each of the
	 * system elements can be retreived by adding their specific index
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
    protected void reportGenErr(String msg) {
        log().warn("Error retrieving systemGroup from "+m_address+". "+msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        log().info("Error retrieving systemGroup from "+m_address+". "+msg);
    }

    private final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
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
    public int getNumberOfPorts() {
    	Integer nop = m_store.getInt32(BASE_NUM_PORTS);
    	if (nop == null) {
            return -1;
        }
    	return nop;
    }

    /**
     * <p>getBridgeType</p>
     *
     * @return a int.
     */
    public int getBridgeType() {
    	Integer type = m_store.getInt32(BASE_NUM_TYPE);
    	if (type == null) {
            return -1;
        }
    	return type;
    }
}
