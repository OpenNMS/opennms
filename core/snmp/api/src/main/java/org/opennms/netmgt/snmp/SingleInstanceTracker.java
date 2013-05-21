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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleInstanceTracker extends CollectionTracker {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(SingleInstanceTracker.class);

    private SnmpObjId m_base;
    private SnmpInstId m_inst;
    private SnmpObjId m_oid;
    
    public SingleInstanceTracker(SnmpObjId base, SnmpInstId inst) {
        this(base, inst, null);
    }
    
    public SingleInstanceTracker(String baseOid, String instId) {
        this(SnmpObjId.get(baseOid), new SnmpInstId(instId));
    }

    public SingleInstanceTracker(SnmpObjId base, SnmpInstId inst, CollectionTracker parent) {
        super(parent);
        m_base = base;
        m_inst = inst;
        m_oid = SnmpObjId.get(m_base, m_inst);
    }
    
    @Override
    public void setMaxRepetitions(int maxRepititions) {
        // do nothing since we are not a repeater
    }

        @Override
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        if (pduBuilder.getMaxVarsPerPdu() < 1) {
            throw new IllegalArgumentException("maxVarsPerPdu < 1");
        }
        
        SnmpObjId requestOid = m_oid.decrement();
        LOG.debug("Requesting oid following: {}", requestOid);
        pduBuilder.addOid(requestOid);
        pduBuilder.setNonRepeaters(1);
        pduBuilder.setMaxRepetitions(1);
        
        ResponseProcessor rp = new ResponseProcessor() {

            @Override
            public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
                LOG.debug("Processing varBind: {} = {}", responseObjId, val);
                
                if (val.isEndOfMib()) {
                    receivedEndOfMib();
                }

                if (m_oid.equals(responseObjId)) {
                    storeResult(new SnmpResult(m_base, m_inst, val));
                }
                
                setFinished(true);
            }

            @Override
            public boolean processErrors(int errorStatus, int errorIndex) {
                if (errorStatus == NO_ERR) {
                    return false;
                } else if (errorStatus == TOO_BIG_ERR) {
                    throw new IllegalArgumentException("Unable to handle tooBigError for oid request "+m_oid.decrement());
                } else if (errorStatus == GEN_ERR) {
                    reportGenErr("Received genErr requesting oid "+m_oid.decrement()+". Marking column is finished.");
                    errorOccurred();
                    return true;
                } else if (errorStatus == NO_SUCH_NAME_ERR) {
                    reportNoSuchNameErr("Received noSuchName reqeusting oid "+m_oid.decrement()+". Marking column is finished.");
                    errorOccurred();
                    return true;
                } else {
                    throw new IllegalArgumentException("Unexpected error processing oid "+m_oid.decrement()+". Aborting!");
                }
            }
        };
        
        return rp;

    }

    protected void errorOccurred() {
        setFinished(true);
    }

    protected void receivedEndOfMib() {
        setFinished(true);
    }

}
