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
// Modifications:
//
// 2008 May 10: In processErrors, when we throw exceptions or notify of errors,
//              state that the OID shown is the *previous* OID. - dj@opennms.org
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
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TableTracker extends CollectionTracker {
    private RowCallback m_callback;
    private int m_maxRepetitions;
    private List<ColumnTrackerTracker> m_columnTrackerTrackers;
    private List<Queue<SnmpResult>> m_pendingData;
    private int m_columnUses = 0;

    public TableTracker(RowCallback rc, SnmpObjId... ids) {
        this(rc, 2, ids);
    }

    public TableTracker(RowCallback rc, int maxRepetitions, SnmpObjId... ids) {
        m_pendingData = new ArrayList<Queue<SnmpResult>>(ids.length);
        m_columnTrackerTrackers = new ArrayList<ColumnTrackerTracker>(ids.length);
        for (SnmpObjId id : ids) {
            m_pendingData.add(new LinkedBlockingQueue<SnmpResult>());
            
            ColumnTracker ct = new ColumnTracker(id);
            ct.setParent(this);
            m_columnTrackerTrackers.add(new ColumnTrackerTracker(ct));
        }

        setMaxRepetitions(maxRepetitions);
        m_callback = rc;
    }

    public int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    @Override
    public void setMaxRepetitions(int maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }


    @Override
    public boolean isFinished() {
        for (ColumnTrackerTracker ctt : m_columnTrackerTrackers) {
            if (!ctt.getTracker().isFinished()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        if (pduBuilder.getMaxVarsPerPdu() < 1) {
            throw new IllegalArgumentException("maxVarsPerPdu < 1");
        }

        Map<SnmpObjId, ResponseProcessor> processors = new HashMap<SnmpObjId,ResponseProcessor>(pduBuilder.getMaxVarsPerPdu());

        for (ColumnTracker ct : getTrackers(pduBuilder.getMaxVarsPerPdu())) {
            System.err.println("scheduling " + ct);
            processors.put(ct.getBase(), ct.buildNextPdu(pduBuilder));
        }
        
        ResponseProcessor rp = new CombinedColumnResponseProcessor(processors);
        System.err.println("got response processor");
        return rp;
    }

    public void storeResult(SnmpResult res) {
        System.err.println(String.format("TableTracker store result: %s", res));
        if (m_callback != null) {
            for (int i = 0; i < m_columnTrackerTrackers.size(); i++) {
                if (m_columnTrackerTrackers.get(i).getTracker().getBase() == res.getBase()) {
                    m_pendingData.get(i).add(res);
                }
            }
            while (hasRow()) {
                List<SnmpResult> row = getRow();
                System.err.println(String.format("row completed: %s", row));
                m_callback.rowCompleted(row);
            }
        }
        
        super.storeResult(res);
    }

    private boolean hasRow() {
        if (isFinished()) {
            for (Queue<SnmpResult> q : m_pendingData) {
                if (!q.isEmpty()) {
                    return true;
                }
            }
            return false;
        } else {
            for (Queue<SnmpResult> q : m_pendingData) {
                if (q.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }

    private List<SnmpResult> getRow() {
        List<SnmpResult> l = new ArrayList<SnmpResult>(m_columnTrackerTrackers.size());
        for (Queue<SnmpResult> q : m_pendingData) {
            if (q.isEmpty()) {
                l.add(null);
            } else {
                l.add(q.poll());
            }
        }
        return l;
    }
    
    private List<ColumnTracker> getTrackers(int max) {
        List<ColumnTracker> trackers = new ArrayList<ColumnTracker>(max);
        
        for (int i = 0; i < m_columnTrackerTrackers.size(); i++) {
            if (trackers.size() >= max) {
                return trackers;
            }
            ColumnTrackerTracker ctt = m_columnTrackerTrackers.get(i);
            ColumnTracker ct = ctt.getTracker();
            if (!ct.isFinished() && ctt.getUses() < m_columnUses) {
                trackers.add(ct);
                ctt.use();
            }
        }
        if (trackers.size() == 0) {
            for (int i = 0; i < m_columnTrackerTrackers.size(); i++) {
                if (trackers.size() >= max) {
                    return trackers;
                }
                ColumnTrackerTracker ctt = m_columnTrackerTrackers.get(i);
                ColumnTracker ct = ctt.getTracker();
                if (!ct.isFinished()) {
                    trackers.add(ct);
                    ctt.use();
                }
            }
        }

        return trackers;
    }
    
    private class CombinedColumnResponseProcessor implements ResponseProcessor {
        private final Map<SnmpObjId, ResponseProcessor> m_processors;

        public CombinedColumnResponseProcessor(Map<SnmpObjId, ResponseProcessor> processors) {
            m_processors = processors;
        }
        
        public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
            System.err.println(String.format("processResponse: %s/%s", responseObjId, val));

            for (SnmpObjId id : m_processors.keySet()) {
                if (id.isPrefixOf(responseObjId) && !id.equals(responseObjId)) {
                    System.err.println(String.format("matched base %s with response object %s", id, responseObjId));
                    ResponseProcessor rp = m_processors.get(id);
                    rp.processResponse(responseObjId, val);
                    return;
                }
            }
            
            System.err.println("no match");
            throw new RuntimeException("holy crap, no match!");
        }

        public boolean processErrors(int errorStatus, int errorIndex) {
            return false;
        }

    }

    private class ColumnTrackerTracker {
        ColumnTracker m_columnTracker;
        private int m_uses = 0;
        
        public ColumnTrackerTracker(ColumnTracker tracker) {
            m_columnTracker = tracker;
        }

        public void use() {
            m_uses++;
            m_columnUses = Math.max(m_uses, m_columnUses);
        }
        
        public int getUses() {
            return m_uses;
        }
        
        public ColumnTracker getTracker() {
            return m_columnTracker;
        }
    }
    
}
