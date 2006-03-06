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
// SnmpIfCollector.java,v 1.1.1.1 2001/11/11 17:34:38 ben Exp
//

package org.opennms.netmgt.collectd;

import java.lang.Integer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

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
    private Map m_results = new TreeMap();
    
    /**
     * Holds the IP Address of the primary SNMP iterface.
     */
    private String m_primaryIf;

    private List m_objList;

    /**
     * The mib object for MIB-2 ifXtable ifAlias
     */
    private static MibObject ifAliasMibObject() {
        MibObject m_ifAliasMibObject = new MibObject();
        m_ifAliasMibObject.setOid(".1.3.6.1.2.1.31.1.1.1.18");
        m_ifAliasMibObject.setAlias("ifAlias");
        m_ifAliasMibObject.setType("DisplayString");
        m_ifAliasMibObject.setInstance("ifIndex");
	return m_ifAliasMibObject;
    }

    /**
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</EM> using the notifyAll() method.
     * @param address 
     * @param ifMap
     *            Map of org.opennms.netmgt.poller.collectd.IfInfo objects.
     */
    public SnmpIfCollector(InetAddress address, Map ifMap) {
        super(MibObject.getCollectionTrackers(SnmpIfCollector.buildV2CombinedOidList(ifMap)));

        // Process parameters
        //
        m_primaryIf = address.getHostAddress();
        m_objList = SnmpIfCollector.buildV2CombinedOidList(ifMap);


    }

    protected static Category log() {
        return ThreadCategory.getInstance(SnmpIfCollector.class);
    }

    /**
     * Returns the list of all entry maps that can be used to access all the
     * information from the service polling.
     */

    public List getEntries() {
        return new ArrayList(m_results.values());
    }
    
    /**
     * This method is responsible for building a new object list consisting of
     * all unique oids to be collected for all interfaces represented within the
     * interface map. The new list can then be used for SNMPv2 collection.
     * 
     * @param ifMap
     *            Map of IfInfo objects indexed by ifIndex
     * 
     * @return unified MibObject list
     */
    private static List buildV2CombinedOidList(Map ifMap) {
        List allOids = new ArrayList();

        // Iterate over all the interface's in the interface map
        //
        if (ifMap != null) {
            Iterator i = ifMap.values().iterator();
            while (i.hasNext()) {
                IfInfo ifInfo = (IfInfo) i.next();
                List ifOidList = ifInfo.getOidList();

                // Add unique interface oid's to the list
                //
                Iterator j = ifOidList.iterator();
                while (j.hasNext()) {
                    MibObject oid = (MibObject) j.next();
                    if (!allOids.contains(oid))
                        allOids.add(oid);
                }
            }
	    allOids.add(ifAliasMibObject());
        }

        return allOids;
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

    protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        SNMPCollectorEntry entry = (SNMPCollectorEntry)m_results.get(inst);
        if (entry == null) {
            entry = new SNMPCollectorEntry(m_objList);
            m_results.put(inst, entry);
        }
        entry.storeResult(base, inst, val);

    }
}
