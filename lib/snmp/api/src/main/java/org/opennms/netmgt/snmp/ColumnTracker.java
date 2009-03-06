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

import org.apache.commons.lang.builder.ToStringBuilder;

public class ColumnTracker extends CollectionTracker {
    
    private SnmpObjId m_base;
    private SnmpObjId m_last;
    private int m_maxRepetitions;

    public ColumnTracker(SnmpObjId base) {
        this(null, base);
    }

    public ColumnTracker(SnmpObjId base, int maxRepititions) {
        this(null, base, maxRepititions);
    }
    
    public ColumnTracker(CollectionTracker parent, SnmpObjId base) {
        this(parent, base, 2);
    }

    public ColumnTracker(CollectionTracker parent, SnmpObjId base, int maxRepititions) {
        super(parent);
        m_base = base;
        m_last = base;
        m_maxRepetitions = maxRepititions;
    }

    public SnmpObjId getBase() {
        return m_base;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("base", m_base)
            .append("last oid", m_last)
            .append("max repetitions", m_maxRepetitions)
            .append("finished?", isFinished())
            .toString();
    }
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        if (pduBuilder.getMaxVarsPerPdu() < 1) {
            throw new IllegalArgumentException("maxVarsPerPdu < 1");
        }

        pduBuilder.addOid(m_last);
        pduBuilder.setNonRepeaters(0);
        pduBuilder.setMaxRepetitions(getMaxRepetitions());
        
        ResponseProcessor rp = new ResponseProcessor() {

            public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
                if (val.isEndOfMib()) {
                    receivedEndOfMib();
                }

                m_last = responseObjId;
                if (m_base.isPrefixOf(responseObjId) && !m_base.equals(responseObjId)) {
                    SnmpInstId inst = responseObjId.getInstance(m_base);
                    if (inst != null) {
                        storeResult(new SnmpResult(m_base, inst, val));
                    }
                }
                
                if (!m_base.isPrefixOf(m_last)) {
                    setFinished(true);
                }
                
            }

            public boolean processErrors(int errorStatus, int errorIndex) {
                if (errorStatus == NO_ERR) {
                    return false;
                } else if (errorStatus == TOO_BIG_ERR) {
                    throw new IllegalArgumentException("Unable to handle tooBigError for next oid request after "+m_last);
                } else if (errorStatus == GEN_ERR) {
                    reportGenErr("Received genErr reqeusting next oid after "+m_last+". Marking column is finished.");
                    errorOccurred();
                    return true;
                } else if (errorStatus == NO_SUCH_NAME_ERR) {
                    reportNoSuchNameErr("Received noSuchName reqeusting next oid after "+m_last+". Marking column is finished.");
                    errorOccurred();
                    return true;
                } else {
                    throw new IllegalArgumentException("Unexpected error processing next oid after "+m_last+". Aborting!");
                }
            }
        };
        
        return rp;
    }

    public int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    @Override
    public void setMaxRepetitions(int maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }

    protected void receivedEndOfMib() {
        setFinished(true);
    }

    protected void errorOccurred() {
        setFinished(true);
    }

    public SnmpInstId getLastInstance() {
        if (m_base.isPrefixOf(m_last) && !m_base.equals(m_last)) {
            return m_last.getInstance(m_base);
        } else {
            return null;
        }
    }
}
