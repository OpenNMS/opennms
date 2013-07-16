/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;

abstract public class SnmpTable<T extends SnmpTableEntry> extends AggregateTracker {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpTable.class);
    private Map<SnmpInstId, T> m_results = new TreeMap<SnmpInstId, T>();
    private InetAddress m_address;
    private String m_tableName;

    /**
     * <p>Constructor for SnmpTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param tableName a {@link java.lang.String} object.
     * @param columns an array of {@link org.opennms.netmgt.provision.service.snmp.NamedSnmpVar} objects.
     * @param <T> a T object.
     */
    protected SnmpTable(InetAddress address, String tableName, NamedSnmpVar[] columns) {
        this(address, tableName, columns, null);
    }
    /**
     * <p>Constructor for SnmpTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param tableName a {@link java.lang.String} object.
     * @param columns an array of {@link org.opennms.netmgt.provision.service.snmp.NamedSnmpVar} objects.
     * @param instances a {@link java.util.Set} object.
     */
    protected SnmpTable(InetAddress address, String tableName, NamedSnmpVar[] columns, Set<SnmpInstId> instances) {
        super(NamedSnmpVar.getTrackersFor(columns, instances));
        m_address = address;
        m_tableName = tableName;
    }
    
    /** {@inheritDoc} */
    @Override
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
     * <p>getInstances</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<SnmpInstId> getInstances() {
        return m_results.keySet();
    }
    
    /**
     * <p>getEntries</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<T> getEntries() {
        return new ArrayList<T>(m_results.values());
    }
    /** {@inheritDoc} */
    @Override
    protected void reportGenErr(String msg) {
        LOG.warn("Error retrieving {} from {}. {}", msg, m_tableName, m_address);
    }
    
    /**
     * <p>getEntry</p>
     *
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @return a T object.
     */
    public T getEntry(SnmpInstId inst) {
        if (failed()) {
            return null;
        }
        return m_results.get(inst);
    }
    
    /**
     * <p>getEntry</p>
     *
     * @param inst a int.
     * @return a T object.
     */
    public T getEntry(int inst) {
        return getEntry(new SnmpInstId(inst));
    }

    /** {@inheritDoc} */
    @Override
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving {} from {}. {}", msg, m_tableName, m_address);
    }
}
