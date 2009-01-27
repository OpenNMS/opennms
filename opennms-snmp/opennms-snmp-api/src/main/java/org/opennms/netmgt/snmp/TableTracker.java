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
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TableTracker extends CollectionTracker {
    private SnmpObjId[] m_ids;
    private SnmpObjId[] m_lastOid;
    private int m_nextColumnIndex = 0;
    private RowCallback m_callback;
    private List<Boolean> m_finishedColumns;
    private List<Queue<SnmpResult>> m_pendingData;
    private int m_maxRepetitions;

    public TableTracker(RowCallback rc, SnmpObjId... ids) {
        this(rc, 2, ids);
    }

    public TableTracker(RowCallback rc, int maxRepetitions, SnmpObjId... ids) {
        System.err.println(String.format("instantiating table tracker: rc=%s, repetitions=%d, object IDs = %s", rc, maxRepetitions, Arrays.toString(ids)));
        m_ids = ids;
        m_lastOid = new SnmpObjId[ids.length];
        m_pendingData = new ArrayList<Queue<SnmpResult>>(ids.length);
        m_finishedColumns = new ArrayList<Boolean>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            m_lastOid[i] = ids[i];
            m_pendingData.add(new LinkedBlockingQueue<SnmpResult>());
            m_finishedColumns.add(false);
        }

        setMaxRepetitions(maxRepetitions);
        m_callback = rc;
    }

    public int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    @Override
    public void setMaxRepetitions(int maxRepetitions) {
        System.err.println("setting max repetitions to " + maxRepetitions);
        m_maxRepetitions = maxRepetitions;
    }


    @Override
    public boolean isFinished() {
        for (Boolean b : m_finishedColumns) {
            if (!b) {
                System.err.println("isFinished: false");
                return false;
            }
        }
        System.err.println("isFinished: true");
        return true;
    }

    @Override
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        if (pduBuilder.getMaxVarsPerPdu() < 1) {
            throw new IllegalArgumentException("maxVarsPerPdu < 1");
        }

        pduBuilder.addOid(m_lastOid[m_nextColumnIndex]);
        pduBuilder.setNonRepeaters(0);
        pduBuilder.setMaxRepetitions(getMaxRepetitions());
        
        ResponseProcessor rp = new TableResponseProcessor(m_nextColumnIndex);
        System.err.println("got response processor");

        m_nextColumnIndex++;
        return rp;
    }

    protected void receivedEndOfMib(int columnIndex) {
        System.err.println("received end of MIB: " + columnIndex);
        m_finishedColumns.set(columnIndex, true);
    }

    protected void errorOccurred(int columnIndex) {
        System.err.println("error occurred: " + columnIndex);
        m_finishedColumns.set(columnIndex, true);
    }

    protected boolean hasRow() {
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

    public List<SnmpResult> getRow() {
        List<SnmpResult> l = new ArrayList<SnmpResult>(m_ids.length);
        for (Queue<SnmpResult> q : m_pendingData) {
            if (q.isEmpty()) {
                l.add(null);
            } else {
                l.add(q.poll());
            }
        }
        return l;
    }
    
    public void storeResult(int columnIndex, SnmpResult res) {
        System.err.println(String.format("store result: column[%s]: storing %s", columnIndex, res));
        
        if (m_callback != null) {
            System.err.println(String.format("pending data: column[%s]: adding %s", columnIndex, res));
            m_pendingData.get(columnIndex).add(res);

            while (hasRow()) {
                List<SnmpResult> row = getRow();
                System.err.println(String.format("pending data: row completed: %s", row));
                m_callback.rowCompleted(row);
            }
        }
        
        if (m_nextColumnIndex > m_ids.length) {
            m_nextColumnIndex = 0;
        }

        super.storeResult(res);
    }
    
    private final class TableResponseProcessor implements ResponseProcessor {
        private final int m_columnIndex;
        
        public TableResponseProcessor(int columnIndex) {
            System.err.println(String.format("instantiating table response processor for index %d", columnIndex));
            m_columnIndex = columnIndex;
        }

        public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
            System.err.println(String.format("processResponse: %s/%s", responseObjId, val));
            
            if (val.isEndOfMib()) {
                receivedEndOfMib(m_columnIndex);
            }

            m_lastOid[m_columnIndex] = responseObjId;
            SnmpObjId base = m_ids[m_columnIndex];
            if (base.isPrefixOf(responseObjId) && !base.equals(responseObjId)) {
                SnmpInstId inst = responseObjId.getInstance(base);
                if (inst != null) {
                    storeResult(m_columnIndex, new SnmpResult(base, inst, val));
                }
            }

        }

        public boolean processErrors(int errorStatus, int errorIndex) {
            System.err.println(String.format("processing error: errorStatus=%d, errorIndex=%d", errorStatus, errorIndex));
            if (errorStatus == NO_ERR) {
                return false;
            } else if (errorStatus == TOO_BIG_ERR) {
                throw new IllegalArgumentException("Unable to handle tooBigError for next oid request after "+m_lastOid[m_columnIndex]);
            } else if (errorStatus == GEN_ERR) {
                reportGenErr("Received genErr reqeusting next oid after "+m_lastOid[m_columnIndex]+". Marking column is finished.");
                errorOccurred(m_columnIndex);
                return true;
            } else if (errorStatus == NO_SUCH_NAME_ERR) {
                reportNoSuchNameErr("Received noSuchName reqeusting next oid after "+m_lastOid[m_columnIndex]+". Marking column is finished.");
                errorOccurred(m_columnIndex);
                return true;
            } else {
                throw new IllegalArgumentException("Unexpected error processing next oid after "+m_lastOid[m_columnIndex]+". Aborting!");
            }
        }
    }

}
