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
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;

public abstract class SnmpTable<T extends SnmpTableEntry> extends AggregateTracker {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpTable.class);
    private Map<SnmpInstId, T> m_results = new TreeMap<SnmpInstId, T>();
    private InetAddress m_address;
    private String m_tableName;

    /**
     * <p>Constructor for SnmpTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param tableName a {@link java.lang.String} object.
     * @param columns an array of {@link org.opennms.netmgt.snmp.NamedSnmpVar} objects.
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
     * @param columns an array of {@link org.opennms.netmgt.snmp.NamedSnmpVar} objects.
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
    
    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving {} from {}. {}", m_tableName, m_address, ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) encountered retrieving {} from {}. {}", status, m_tableName, m_address, status.retry()? "Retrying." : "Giving up.");
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
