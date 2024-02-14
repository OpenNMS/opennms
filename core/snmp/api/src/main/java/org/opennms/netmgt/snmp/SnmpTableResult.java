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
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brozow
 */
public class SnmpTableResult implements RowResultFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpTableResult.class);

    private final RowCallback m_callback;
    private final SnmpObjId[] m_columns;
    private final RowResultFactory m_rowResultFactory;

    private final List<SnmpObjId> m_finishedColumns;
    private final Map<SnmpInstId,SnmpRowResult> m_pendingData;
    private volatile boolean m_finished = false;
    
    public SnmpTableResult(RowCallback callback, SnmpObjId... columns) {
        this(callback, null, columns);
    }
 
    public SnmpTableResult(RowCallback callback, RowResultFactory rowResultFactory, SnmpObjId... columns) {
        m_callback = callback;
        m_columns = columns;
        m_rowResultFactory = (rowResultFactory == null ? this : rowResultFactory);

        m_finishedColumns = new ArrayList<>();
        m_pendingData = new TreeMap<SnmpInstId,SnmpRowResult>();
    }
    
    private int getColumnCount() {
        return m_columns.length;
    }

    void storeResult(SnmpResult result) {
        SnmpInstId instId = result.getInstance();
        if ( !m_pendingData.containsKey( instId ) ) {
            m_pendingData.put( instId, m_rowResultFactory.createRowResult( getColumnCount(), instId ) );
        }
        SnmpRowResult row = m_pendingData.get( instId );
        row.addResult( result.getBase(), result );

        handleCompleteRows();
    }

    public void setFinished(boolean finished) {
        m_finished = finished;
    }

    public boolean isFinished() {
        return m_finished;
    }

    void handleCompleteRows() {
        SnmpInstId lastInstance = null;

        for (SnmpRowResult row : m_pendingData.values()) {
            if (row.isComplete(m_finishedColumns.toArray(new SnmpObjId[m_finishedColumns.size()]))) {
                lastInstance = row.getInstance();
            }
        }
        
        if (lastInstance != null || isFinished()) {
            Iterator<SnmpInstId> i = m_pendingData.keySet().iterator();
            while (i.hasNext()) {
                final SnmpInstId key = i.next();
                final SnmpRowResult pendingData = m_pendingData.get(key);
                try {
                    m_callback.rowCompleted(pendingData);
                } catch (final Exception e) {
                    LOG.warn("Failed to handle completed SNMP table row {}: {}", key, pendingData, e);
                }
                i.remove();
                if (key.equals(lastInstance)) {
                    break;
                }
            }
        }
    }

    public void tableFinished() {
        setFinished(true);
        handleCompleteRows();
    }

    public void columnFinished(SnmpObjId columnId) {
        m_finishedColumns.add(columnId);
        handleCompleteRows();
    }

    @Override
    public SnmpRowResult createRowResult(int columnCount, SnmpInstId instance) {
        return new SnmpRowResult(columnCount, instance);
    }

}
