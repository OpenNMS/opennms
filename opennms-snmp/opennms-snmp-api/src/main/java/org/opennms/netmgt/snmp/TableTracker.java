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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.CompareToBuilder;

public class TableTracker extends CollectionTracker {
    private RowCallback m_callback;
    private int m_maxRepetitions;
    private List<ColumnTracker> m_columnTrackers;
    private Map<SnmpInstId,SnmpRowResult> m_pendingData;

    public TableTracker(RowCallback rc, SnmpObjId... ids) {
        this(rc, 2, ids);
    }

    public TableTracker(RowCallback rc, int maxRepetitions, SnmpObjId... ids) {
        m_pendingData = new TreeMap<SnmpInstId,SnmpRowResult>();
        m_columnTrackers = new ArrayList<ColumnTracker>(ids.length);
        for (SnmpObjId id : ids) {
            ColumnTracker ct = new ColumnTracker(id);
            ct.setParent(this);
            m_columnTrackers.add(ct);
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
        for (ColumnTracker ct : m_columnTrackers) {
            if (!ct.isFinished()) {
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

        List<ResponseProcessorTracker> processors = new ArrayList<ResponseProcessorTracker>(pduBuilder.getMaxVarsPerPdu());

        for (ColumnTracker ct : getTrackers(pduBuilder.getMaxVarsPerPdu())) {
            processors.add(new ResponseProcessorTracker(ct, ct.buildNextPdu(pduBuilder)));
        }

        ResponseProcessor rp = new CombinedColumnResponseProcessor(processors);
        return rp;
    }

    public void storeResult(SnmpResult res) {
        super.storeResult(res);

        System.err.println(String.format("storeResult: %s", res));
        if (m_callback != null) {

            int columnInstance = res.getBase().getLastSubId();
            if (!m_pendingData.containsKey(res.getInstance())) {
                m_pendingData.put(res.getInstance(), new SnmpRowResult(m_columnTrackers.size()));
            }
            SnmpRowResult row = m_pendingData.get(res.getInstance());
            row.setResult(columnInstance, res);

            while (hasRow()) {
                row = getNextRow();
                if (row != null) {
                    System.err.println(String.format("rowCompleted: %s", row));
                    m_callback.rowCompleted(row);
                }
            }
        }
    }

    private boolean hasRow() {
        if (isFinished()) {
            if (!m_pendingData.isEmpty()) {
                return true;
            }
            return false;
        } else {
            for (SnmpRowResult rr : m_pendingData.values()) {
                if (rr.isComplete()) {
                    return true;
                }
            }
            return false;
        }
    }

    private SnmpRowResult getNextRow() {
        for (SnmpInstId id : m_pendingData.keySet()) {
            if (m_pendingData.get(id).isComplete() || isFinished()) {
                return m_pendingData.remove(id);
            }
        }
        return null;
    }
    
    private List<ColumnTracker> getTrackers(int max) {
        List<ColumnTracker> trackers = new ArrayList<ColumnTracker>(max);
        List<ColumnTracker> trackerList = new ArrayList<ColumnTracker>(m_columnTrackers);

        Collections.sort(trackerList, new Comparator<ColumnTracker>() {
            public int compare(ColumnTracker o1, ColumnTracker o2) {
                return new CompareToBuilder()
                    .append(o1.getCurrentRow(), o2.getCurrentRow())
                    .append(o1.getBase(), o2.getBase())
                    .toComparison();
            }
        });

        for (int i = 0; i < trackerList.size(); i++) {
            if (trackers.size() >= max) {
                return trackers;
            }
            ColumnTracker ct = trackerList.get(i);
            if (!ct.isFinished()) {
                System.err.println(String.format("index %d: using tracker %s", i, ct));
                trackers.add(ct);
            }
        }

        return trackers;
    }
    
    private class ResponseProcessorTracker {
        private final ColumnTracker m_tracker;
        private final ResponseProcessor m_processor;
        
        public ResponseProcessorTracker(ColumnTracker ct, ResponseProcessor rp) {
            m_tracker = ct;
            m_processor = rp;
        }
        
        public ColumnTracker getColumnTracker() {
            return m_tracker;
        }
        
        public ResponseProcessor getResponseProcessor() {
            return m_processor;
        }
    }

    private class CombinedColumnResponseProcessor implements ResponseProcessor {
        private final List<ResponseProcessorTracker> m_processors;
        private int m_currentIndex = 0;

        public CombinedColumnResponseProcessor(List<ResponseProcessorTracker> processors) {
            m_processors = processors;
        }

        public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
            ResponseProcessor rp = m_processors.get(m_currentIndex).getResponseProcessor();
            ColumnTracker ct = m_processors.get(m_currentIndex).getColumnTracker();
            
            if (++m_currentIndex == m_processors.size()) {
                m_currentIndex = 0;
            }

            System.err.println(String.format("processResponse: trying: index(%d): tracker=%s, responseObj=%s, value=%s", m_currentIndex, ct, responseObjId, val));
            rp.processResponse(responseObjId, val);
        }

        public boolean processErrors(int errorStatus, int errorIndex) {
            ResponseProcessor rp = m_processors.get(m_currentIndex).getResponseProcessor();
            ColumnTracker ct = m_processors.get(m_currentIndex).getColumnTracker();
            
            if (++m_currentIndex == m_processors.size()) {
                m_currentIndex = 0;
            }

            System.err.println(String.format("processError: trying: index(%d): tracker=%s, errorStatus=%d, errorIndex=%d", m_currentIndex, ct, errorStatus, errorIndex));
            return rp.processErrors(errorStatus, errorIndex);
        }

    }

}
