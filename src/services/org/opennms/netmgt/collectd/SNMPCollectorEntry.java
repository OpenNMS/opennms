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

import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>
 * The SNMPCollectorEntry class is designed to hold all SNMP collected data
 * pertaining to a particular interface.
 * </P>
 * 
 * <P>
 * An instance of this class is created by calling the constructor and passing a
 * list of SnmpVarBind objects from an SNMP PDU response. This class extends
 * java.util.TreeMap which is used to store each of the collected data points
 * indexed by object identifier.
 * </P>
 * 
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 */
public final class SNMPCollectorEntry extends java.util.TreeMap {
    /**
     * The list of MIBObjects that will used for associating the the data within
     * the map.
     */
    private java.util.List m_objList;

    /**
     * Key that will be used in the map for returning the ifIndex of particular
     * interface.
     */
    public static final String IF_INDEX = "ifIndex";

    /**
     * <P>
     * Creates a default instance of the SNMPCollector entry map. The map
     * represents a singular instance from the MibObject. Each column in the
     * table for the loaded instance may be retrieved through its OID from the
     * MIBObject.
     * </P>
     * 
     * <P>
     * The initial table is constructed with zero elements in the map.
     * </P>
     */
    public SNMPCollectorEntry() {
        super();
        m_objList = null;
    }

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's member variables can change after an instance is
     * created, this constructor will initialize all the variables as per their
     * named variable from the passed array of SNMP varbinds.
     * </P>
     * 
     * <P>
     * If the information in the object should not be modified then a <EM>final
     * </EM> modifier can be applied to the created object.
     * </P>
     * 
     * @param vars
     *            The array of collected SNMP variable bindings
     * @param objList
     *            List of MibObject objects representing each of of the oid's
     *            configured for collection.
     * @param ifIndex
     *            The ifIndex (as a String) of the interface for which the
     *            collected SNMP data is relevant. NOTE: NULL if the collected
     *            SNMP data is for the node.
     */
    public SNMPCollectorEntry(SnmpVarBind[] vars, List objList, String ifIndex) {
        this();
        m_objList = objList;

        // Store the ifIndex to which the varbind list pertains
        // within the map. This provides an easy mechanism for
        // determining which interface a particular SNMPCollectorEntry
        // applies to.
        if (ifIndex != null)
            put(IF_INDEX, ifIndex);

        // Store the collected data within this entry's map.
        update(vars, ifIndex);
    }

    /**
     * <P>
     * This method is used to update this entry's map with the current
     * information from the agent.
     * 
     * </P>
     * This does not clear out any column in the actual row that does not have a
     * definition.
     * </P>
     * 
     * @param vars
     *            Array of SnmpVarBind objects containing all the SNMP data
     *            collected for a particular interface.
     * @param ifIndex
     *            The ifIndex (as a String) of the interface for which the
     *            collected SNMP data is relevant.
     * 
     */
    public void update(SnmpVarBind[] vars, String ifIndex) {
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("update: updating SNMPCollectorEntry map for ifIndex: " + ifIndex);

        try {
            // Iterate over the list of MibObjects representing the
            // objects configured for collection. For each MIB object
            // iterate over the SnmpVarBind array looking for the matching
            // variable. If a match is found, insert the SNMP-retrieved value
            // into the entry's map indexed by its object identifier.
            //
            for (int x = 0; x < m_objList.size(); x++) {
                MibObject mibObject = (MibObject) m_objList.get(x);

                // Build fullOid.
                //
                String instance = null;
                if (mibObject.getInstance().equals(MibObject.INSTANCE_IFINDEX))
                    instance = ifIndex;
                else
                    instance = mibObject.getInstance();

                String fullOid = mibObject.getOid() + "." + instance;

                SnmpObjectId id = new SnmpObjectId(fullOid);

                for (int y = 0; y < vars.length; y++) {
                    if (vars[y] != null && id.isRootOf(vars[y].getName())) {
                        try {
                            put(fullOid, vars[y].getValue());
                            if (log.isDebugEnabled())
                                log.debug("update: added oid:value pair: " + fullOid + " : " + vars[y].getValue());
                        } catch (NullPointerException e) {
                            if (log.isDebugEnabled())
                                log.debug("update: a null pointer exception occured", e);
                        }

                        break;
                    }
                }
            }
        } catch (Throwable t) {
            if (log.isEnabledFor(Priority.WARN))
                log.warn("update: unexpected exception: ", t);
        }
    }
}
