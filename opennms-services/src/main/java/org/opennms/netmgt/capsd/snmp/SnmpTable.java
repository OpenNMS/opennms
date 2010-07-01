//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;


/**
 * <p>Abstract SnmpTable class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
abstract public class SnmpTable<T extends SnmpTableEntry> extends AggregateTracker {
    
    private Map<SnmpInstId, T> m_results = new TreeMap<SnmpInstId, T>();
    private InetAddress m_address;
    private String m_tableName;

    /**
     * <p>Constructor for SnmpTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param tableName a {@link java.lang.String} object.
     * @param columns an array of {@link org.opennms.netmgt.capsd.snmp.NamedSnmpVar} objects.
     * @param <T> a T object.
     */
    protected SnmpTable(InetAddress address, String tableName, NamedSnmpVar[] columns) {
        super(NamedSnmpVar.getTrackersFor(columns));
        m_address = address;
        m_tableName = tableName;
    }
    
    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        T entry = m_results.get(res.getInstance());
        if (entry == null) {
            entry = createTableEntry(res.getBase(), res.getInstance(), res.getValue());
            m_results.put(res.getInstance(), entry);
        }
        entry.storeResult(res);
    }

    /**
     * <p>createTableEntry</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @param val a {@link java.lang.Object} object.
     * @return a T object.
     */
    protected abstract T createTableEntry(SnmpObjId base, SnmpInstId inst, Object val);

    /**
     * <p>getEntries</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<T> getEntries() {
        return new ArrayList<T>(m_results.values());
    }
    /** {@inheritDoc} */
    protected void reportGenErr(String msg) {
        log().warn("Error retrieving "+m_tableName+" from "+m_address+". "+msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        log().info("Error retrieving "+m_tableName+" from "+m_address+". "+msg);
    }
    
    private final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }


}
