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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;

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
public final class CdpGlobalGroupTracker extends AggregateTracker
{
	private final static Logger LOG = LoggerFactory.getLogger(CdpGlobalGroupTracker.class);
    /**
     * the bridge type
     */
	//
	// Lookup strings for specific table entries
	//
	public final static	String	CDP_GLOBAL_RUN	= "cdpGlobalRun";
	public final static	String	CDP_GLOBAL_DEVICEID	= "cdpGlobalDeviceId";
	
	public final static NamedSnmpVar[] ms_elemList = new NamedSnmpVar[] {
		
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,CDP_GLOBAL_RUN,".1.3.6.1.4.1.9.9.23.1.3.1"),
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
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,CDP_GLOBAL_DEVICEID,".1.3.6.1.4.1.9.9.23.1.3.4")

	};

	/**
	 * <P>The SYSTEM_OID is the object identifier that represents the
	 * root of the system information in the MIB forest. Each of the
	 * system elements can be retrieved by adding their specific index
	 * to the string, and an additional Zero(0) to signify the single 
	 * instance item.</P>
	 */
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
	public CdpGlobalGroupTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList); 
	}

    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(final String msg) {
        LOG.warn("Error retrieving systemGroup: {}", msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(final String msg) {
        LOG.info("Error retrieving systemGroup: {}", msg);
    }

    /**
     * <p>getCdpDeviceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCdpDeviceId() {
        return m_store.getDisplayString(CDP_GLOBAL_DEVICEID);
    }
    
    public Integer getCdpGlobalRun() {
    	return m_store.getInt32(CDP_GLOBAL_RUN);
    }
    
    public CdpElement getCdpElement() {
    	CdpElement cdpElement = new CdpElement();
    	cdpElement.setCdpGlobalRun(TruthValue.get(getCdpGlobalRun()));
    	cdpElement.setCdpGlobalDeviceId(getCdpDeviceId());
    	return cdpElement;
    }
        
}
