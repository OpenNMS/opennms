/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author brozow
 *
 */
public class SnmpTableResult implements RowResultFactory {

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

        m_finishedColumns = new ArrayList<SnmpObjId>();
        m_pendingData = new TreeMap<SnmpInstId,SnmpRowResult>();
    }
    
    private int getColumnCount() {
        return m_columns.length;
    }

    /**
     * @param result
     */
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
                SnmpInstId key = i.next();
                m_callback.rowCompleted(m_pendingData.get(key));
                i.remove();
                if (key.equals(lastInstance)) {
                    break;
                }
            }
        }
    }

    void tableFinished() {
        setFinished(true);
        handleCompleteRows();
    }

    /**
     * @param base
     */
    public void columnFinished(SnmpObjId columnId) {
        m_finishedColumns.add(columnId);
        handleCompleteRows();
    }

    @Override
    public SnmpRowResult createRowResult(int columnCount, SnmpInstId instance) {
        return new SnmpRowResult(columnCount, instance);
    }

}
