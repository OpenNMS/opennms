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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * The SnmpNodeCollector class is responsible for performing the actual SNMP
 * data collection for a node over a specified network interface. The
 * SnmpNodeCollector implements the SnmpHandler class in order to receive
 * notifications when an SNMP reply is received or error occurs.
 *
 * The SnmpNodeCollector is provided a list of MIB objects to collect and an
 * interface over which to collect the data. Data collection can be via SNMPv1
 * GetNext requests or SNMPv2 GetBulk requests depending upon the parms used to
 * construct the collector.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 */
public class SnmpNodeCollector extends AggregateTracker {
    /**
     * Used to store the collected MIB data.
     */
    private SNMPCollectorEntry m_collectorEntry;

    /**
     * Holds the IP Address of the primary SNMP iterface.
     */
    private String m_primaryIf;

    private SnmpCollectionSet m_collectionSet;

    /**
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</EM> using the notifyAll() method.
     *
     * @param address TODO
     * @param objList
     *            The list of object id's to be collected.
     * @param collectionSet TODO
     */
    public SnmpNodeCollector(InetAddress address, Collection<SnmpAttributeType> objList, SnmpCollectionSet collectionSet) {
        super(SnmpAttributeType.getCollectionTrackers(objList));
        
        m_primaryIf = address.getHostAddress();
        m_collectionSet = collectionSet;
        m_collectorEntry = new SNMPCollectorEntry(objList, m_collectionSet);


    }


    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * Returns the list of all entry maps that can be used to access all the
     * information from the service polling.
     *
     * @return a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    public SNMPCollectorEntry getEntry() {
        return m_collectorEntry;
    }
    
    /** {@inheritDoc} */
    protected void reportGenErr(String msg) {
        log().warn("genErr collecting data for node "+m_primaryIf+": "+msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        log().info("noSuchName collecting data for node "+m_primaryIf+": "+msg);
    }

    /** {@inheritDoc} */
    protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        m_collectorEntry.storeResult(base, inst, val);
    }


    /**
     * <p>getCollectionSet</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SnmpCollectionSet} object.
     */
    public SnmpCollectionSet getCollectionSet() {
        return m_collectionSet;
    }
}
