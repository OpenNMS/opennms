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
package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SnmpCollectionTracker {
    
    List m_colList;
    int m_lastReceivedColumn = 0;
    private Map m_instanceMaps = new HashMap();

    public SnmpCollectionTracker(List objList) {
        m_colList = new ArrayList(objList.size());
        for (Iterator it = objList.iterator(); it.hasNext();) {
            MibObject mibObj = (MibObject) it.next();
            SnmpColumn col = new SnmpColumn(mibObj.getOid(), mibObj.getInstance());
            m_colList.add(col);
        }
        
    }
    /*
     * TODO: handle getting data out of order
     * TODO: (maybe)be able to start with just the root oid of the table and process an 
     * entire table
     *
     * TODO: handle maxRepeaters greater than 1
     * TODO: handle repition hints
     * TODO: handle hint oids to be retrieved in first packet
     * TODO: handle hint values passed in with initial data
     * 
     */

    public ResponseProcessor buildNextPdu(PduBuilder pduBuilder, int maxVarsPerPdu) {
        final List expectantCols = new ArrayList(Math.min(maxVarsPerPdu, m_colList.size()));
        int count = 0;
        for(int i = 0; i < m_colList.size() && count < maxVarsPerPdu; i++) {
            int index = i;
            SnmpColumn col = (SnmpColumn)m_colList.get(index);
            if (col.hasOidForNext()) {
                count++;
                pduBuilder.addOid(col.getOidForNext());
                expectantCols.add(col);
            }
        }
       
        pduBuilder.setNonRepeaters(expectantCols.size());
        pduBuilder.setMaxRepititions(1);
        return new ResponseProcessor() {
            
            int currIndex = 0;

            public void processResponse(SnmpObjId snmpObjId, Object val) {
                
                SnmpColumn col = (SnmpColumn)expectantCols.get(currIndex);
                SnmpInstId inst = col.addResult(snmpObjId, val);
                if (inst != null) {
                    Map instMap = (Map)m_instanceMaps.get(inst);
                    if (instMap == null) {
                        instMap = new HashMap();
                        m_instanceMaps.put(inst, instMap);
                    }
                    instMap.put(col.getBase(), val);
                }
                currIndex++;
            }
            
        };
    }
    public Map getDataForInstance(SnmpInstId id) {
        return (Map)m_instanceMaps.get(id) ;
    }
    public boolean isFinished() {
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
