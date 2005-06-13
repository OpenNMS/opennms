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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.MibObject;

public class SnmpCollectionTracker {
    
    public static final String COLUMN = "column";
    
    public interface CollectionDefinition {
        public String getOid();
        public String getInstanceDef();
    }
    
    public static final int NO_ERR = 0;
    public static final int TOO_BIG_ERR = 1;
    public static final int NO_SUCH_NAME_ERR = 2;
    public static final int GEN_ERR = 5;
    
    public static final Object END_OF_MIB = new Object() { public String toString() { return "endOfMibView"; } };
    
    class PduResponseProcessor implements ResponseProcessor {
        
        List m_repeaters;
        List m_nonRepeaters;
        
        int m_currIndex = 0;

        public PduResponseProcessor(List nonRepeaters, List repeaters) {
            m_nonRepeaters = nonRepeaters;
            m_repeaters = repeaters;
        }


        public SnmpColumn getColumn() {
            int zeroBasedIndex = m_currIndex;
            return getColumn(zeroBasedIndex);
        }


        private SnmpColumn getColumn(int zeroBasedIndex) {
            if (zeroBasedIndex < m_nonRepeaters.size())
                return (SnmpColumn)m_nonRepeaters.get(zeroBasedIndex);
            int repeaterIndex = (zeroBasedIndex - m_nonRepeaters.size()) % m_repeaters.size();
            return (SnmpColumn)m_repeaters.get(repeaterIndex);
        }
        

        public void processResponse(SnmpObjId snmpObjId, Object val) {
            
            SnmpColumn col = getColumn();
            if (val == END_OF_MIB)
                col.receivedEndOfMib();
            SnmpInstId inst = col.addResult(snmpObjId, val);
            if (inst != null) {
                Map instMap = (Map)m_instanceMaps.get(inst);
                if (instMap == null) {
                    instMap = new HashMap();
                    m_instanceMaps.put(inst, instMap);
                }
                instMap.put(col.getBase(), val);
            }
            m_currIndex++;
        }

        public boolean processErrors(int errorStatus, int errorIndex) {
            switch(errorStatus) {
            case GEN_ERR: {
                SnmpColumn col = getColumn(Math.max(0, errorIndex - 1));
                log().warn("GenErr: processing varbind "+errorIndex+"( "+col.getOidForNext()+" )");
                col.errorOccurred();
                break;
            }
            case TOO_BIG_ERR: {
                m_maxVarsPerPdu = m_maxVarsPerPdu / 2;
                log().warn("TooBig message received reduing maxVarsPerPdu to "+m_maxVarsPerPdu);
                break;
            }
            case NO_SUCH_NAME_ERR: {
                SnmpColumn col = getColumn(Math.max(0, errorIndex - 1));
                log().warn("NoSuchNameErr: processing varbind "+errorIndex+"( "+col.getOidForNext()+" )");
                col.errorOccurred();
                break;
            }
            }
            return errorStatus != NO_ERR;
        }
        
    };
    

    List m_colList;
    int m_lastReceivedColumn = 0;
    int m_maxVarsPerPdu;
    private Map m_instanceMaps = new HashMap();
    

    public SnmpCollectionTracker(List objList, int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
        m_colList = new ArrayList(objList.size());
        for (Iterator it = objList.iterator(); it.hasNext();) {
            CollectionDefinition mibObj = (MibObject) it.next();
            SnmpColumn col = new SnmpColumn(mibObj.getOid(), mibObj.getInstanceDef());
            m_colList.add(col);
        }
        
    }
    /*
     * TODO: handle repition hints
     * TODO: handle hint oids to be retrieved in first packet
     * TODO: handle hint values passed in with initial data
     * 
     */

    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder) {
        
        int maxVars = Math.min(m_maxVarsPerPdu, m_colList.size());
        List nonRepeaters = new ArrayList(maxVars);
        List repeaters = new ArrayList(maxVars);
        int count = 0;
        for(int i = 0; i < m_colList.size() && count < m_maxVarsPerPdu; i++) {
            int index = i;
            SnmpColumn col = (SnmpColumn)m_colList.get(index);
            if (col.hasOidForNext()) {
                count++;
                if (col.isNonRepeater())
                    nonRepeaters.add(col);
                else
                    repeaters.add(col);
            }
        }
        
        for (Iterator it = nonRepeaters.iterator(); it.hasNext();) {
            SnmpColumn col = (SnmpColumn) it.next();
            pduBuilder.addOid(col.getOidForNext());
        }
        
        for (Iterator it = repeaters.iterator(); it.hasNext();) {
            SnmpColumn col = (SnmpColumn) it.next();
            pduBuilder.addOid(col.getOidForNext());
        }
       
        pduBuilder.setNonRepeaters(nonRepeaters.size());
        pduBuilder.setMaxRepititions(10);

        return new PduResponseProcessor(nonRepeaters, repeaters);
    }
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    public Map getDataForInstance(SnmpInstId id) {
        return (Map)m_instanceMaps.get(id) ;
    }
    public boolean isFinished() {
        if (m_maxVarsPerPdu < 1)
            throw new IllegalStateException("Unable to query agent. maxVarsPerPdu is "+m_maxVarsPerPdu);
        for (Iterator it = m_colList.iterator(); it.hasNext();) {
            SnmpColumn col = (SnmpColumn) it.next();
            if (col.hasOidForNext())
                return false;
        }
        return true;
    }
    public Set getInstances() {
        return new HashSet(m_instanceMaps.keySet());
    }



}
