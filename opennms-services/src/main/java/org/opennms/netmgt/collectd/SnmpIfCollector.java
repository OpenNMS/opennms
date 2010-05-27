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
// 2006 Aug 15: Add toString() method. - dj@opennms.org
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
// SnmpIfCollector.java,v 1.1.1.1 2001/11/11 17:34:38 ben Exp
//

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpResult;

/**
 * The SnmpIfCollector class is responsible for performing the actual SNMP data
 * collection for a node over a specified network interface. The SnmpIfCollector
 * implements the SnmpHandler class in order to receive notifications when an
 * SNMP reply is received or error occurs.
 * 
 * The SnmpIfCollector is provided a list of MIB objects to collect and an
 * interface over which to collect the data. Data collection can be via SNMPv1
 * GetNext requests or SNMPv2 GetBulk requests depending upon the parms used to
 * construct the collector.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 */
public class SnmpIfCollector extends AggregateTracker {
    private Map<SnmpInstId, SNMPCollectorEntry> m_results = new TreeMap<SnmpInstId, SNMPCollectorEntry>();
    
    /**
     * Holds the IP Address of the primary SNMP iterface.
     */
    private String m_primaryIf;

    private List<SnmpAttributeType> m_objList;

    private SnmpCollectionSet m_collectionSet;
    
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	
    	buffer.append(getClass().getName());
    	buffer.append("@");
    	buffer.append(Integer.toHexString(hashCode()));
    	
    	buffer.append(": Primary Interface: " + m_primaryIf);
    	buffer.append(", object list: " + m_objList);
    	buffer.append(", CollectionSet: ");
    	if (m_collectionSet == null) {
    		buffer.append("(null)");
    	} else {
    		buffer.append(m_collectionSet.getClass().getName());
    		buffer.append("@");
        	buffer.append(Integer.toHexString(m_collectionSet.hashCode()));
    	}
    	
    	return buffer.toString();
    }

	/**
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</EM> using the notifyAll() method.
     * @param address 
	 * @param objList TODO
	 * @param collectionSet TODO
	 * @param ifMap
     *            Map of org.opennms.netmgt.poller.collectd.IfInfo objects.
     */
    public SnmpIfCollector(InetAddress address, List<SnmpAttributeType> objList, SnmpCollectionSet collectionSet) {
        super(SnmpAttributeType.getCollectionTrackers(objList));
        
        log().debug("COLLECTING on list of "+objList.size()+" items");
        log().debug("List is "+objList);
        // Process parameters
        //
        m_primaryIf = address.getHostAddress();
        m_objList = objList;
        m_collectionSet = collectionSet;
    }

    protected static ThreadCategory log() {
        return ThreadCategory.getInstance(SnmpIfCollector.class);
    }

    /**
     * Returns the list of all entry maps that can be used to access all the
     * information from the service polling.
     */

    public List<SNMPCollectorEntry> getEntries() {
        return new ArrayList<SNMPCollectorEntry>(m_results.values());
    }
    
	protected void reportGenErr(String msg) {
        log().warn(m_primaryIf+": genErr collecting ifData. "+msg);
    }

    protected void reportNoSuchNameErr(String msg) {
        log().info(m_primaryIf+": noSuchName collecting ifData. "+msg);
    }

    protected void reportTooBigErr(String msg) {
        log().info(m_primaryIf+": request tooBig. "+msg);
    }

    protected void storeResult(SnmpResult res) {
        if(res.getBase().toString().equals(SnmpCollector.IFALIAS_OID) && (res.getValue().isNull() || res.getValue().toDisplayString() == null || res.getValue().toDisplayString().equals(""))) {
            log().debug("Skipping storeResult. Null or zero length ifAlias");
            return;
        }
        SNMPCollectorEntry entry = m_results.get(res.getInstance());
        if (entry == null) {
            log().debug("Creating new SNMPCollectorEntry entry");
            entry = new SNMPCollectorEntry(m_objList, m_collectionSet);
            m_results.put(res.getInstance(), entry);
        }
        entry.storeResult(res);

    }
    
    public boolean hasData() {
        return !m_results.isEmpty();
    }
    
    public CollectionSet getCollectionSet() {
        return m_collectionSet;
    }
}
