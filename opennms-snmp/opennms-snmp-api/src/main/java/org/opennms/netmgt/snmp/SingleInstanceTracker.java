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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>SingleInstanceTracker class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SingleInstanceTracker extends CollectionTracker {

    private SnmpObjId m_base;
    private SnmpInstId m_inst;
    private SnmpObjId m_oid;
    private boolean m_finished = false;
    
    /**
     * <p>Constructor for SingleInstanceTracker.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
    public SingleInstanceTracker(SnmpObjId base, SnmpInstId inst) {
        this(base, inst, null);
    }

    /**
     * <p>Constructor for SingleInstanceTracker.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public SingleInstanceTracker(SnmpObjId base, SnmpInstId inst, CollectionTracker parent) {
        super(parent);
        m_base = base;
        m_inst = inst;
        m_oid = SnmpObjId.get(m_base, m_inst);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setMaxRepititions(int maxRepititions) {
        // do nothing since we are not a repeater
    }

    /**
     * <p>isFinished</p>
     *
     * @return a boolean.
     */
    public boolean isFinished() {
        return m_finished;
    }

    /** {@inheritDoc} */
    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        if (pduBuilder.getMaxVarsPerPdu() < 1)
            throw new IllegalArgumentException("maxVarsPerPdu < 1");
        
        SnmpObjId requestOid = m_oid.decrement();
        log().debug("Requesting oid following: "+requestOid);
        pduBuilder.addOid(requestOid);
        pduBuilder.setNonRepeaters(1);
        pduBuilder.setMaxRepetitions(1);
        
        ResponseProcessor rp = new ResponseProcessor() {

            public void processResponse(SnmpObjId responseObjId, SnmpValue val) {
                log().debug("Processing varBind: "+responseObjId+" = "+val);
                
                if (val.isEndOfMib())
                    receivedEndOfMib();

                m_finished = true;
                if (m_oid.equals(responseObjId)) {
                    storeResult(m_base, m_inst, val);
                }
            }

            public boolean processErrors(int errorStatus, int errorIndex) {
                if (errorStatus == NO_ERR) {
                    return false;
                } else if (errorStatus == TOO_BIG_ERR) {
                    throw new IllegalArgumentException("Unable to handle tooBigError for oid request "+m_oid.decrement());
                } else if (errorStatus == GEN_ERR) {
                    reportGenErr("Received genErr reqeusting oid "+m_oid.decrement()+". Marking column is finished.");
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

    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>errorOccurred</p>
     */
    protected void errorOccurred() {
        m_finished = true;
    }

    /**
     * <p>receivedEndOfMib</p>
     */
    protected void receivedEndOfMib() {
        m_finished = true;
    }


}
