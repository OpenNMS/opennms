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
package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Abstract SnmpTable class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class SnmpTable<T extends SnmpStore> extends AggregateTracker implements Collection<T> {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpTable.class);

    private final Map<SnmpInstId, T> m_results = new TreeMap<SnmpInstId, T>();
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
     * <p>getEntries</p>
     *
     * @return a {@link java.util.List} object.
     */
    public Collection<T> getEntries() {
        return new ArrayList<T>(m_results.values());
    }

    /**
     * <p>iterator</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<T> iterator() {
        return m_results.values().iterator();
    }

    /** {@inheritDoc} */
    @Override
    protected void reportGenErr(String msg) {
        LOG.warn("Error retrieving {} from {}. {}", m_tableName, m_address, msg);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving {} from {}. {}", m_tableName, m_address, msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving {} from {}. {}", m_tableName, m_address, ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) encountered: {}", status, status.retry()? "Retrying." : "Giving up.");
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        return m_results.values().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return m_results.values().containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return m_results.values().isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return m_results.values().size();
    }

    @Override
    public Object[] toArray() {
        return m_results.values().toArray();
    }

    @Override
    public <S> S[] toArray(S[] a) {
        return m_results.values().toArray(a);
    }
}
