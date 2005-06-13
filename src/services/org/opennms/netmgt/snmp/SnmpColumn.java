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

import java.util.Map;
import java.util.TreeMap;



public class SnmpColumn {

    private InstanceTracker m_tracker;
    private Map m_results = new TreeMap();

    public SnmpColumn(String baseOid, String instances) {
        this(SnmpObjId.get(baseOid), instances);
    }
    
    public SnmpColumn(SnmpObjId baseOid, String instances) {
        this(InstanceTracker.get(baseOid, instances));
    }
    
    private SnmpColumn(InstanceTracker tracker) {
        m_tracker = tracker;
    }
    
    public SnmpObjId getBase() {
        return m_tracker.getBaseOid();
    }

    public boolean hasOidForNext() {
        return m_tracker.hasOidForNext();
    }

    public SnmpObjId getOidForNext() {
        return m_tracker.getOidForNext();
    }

    public SnmpInstId addResult(SnmpObjId oid, Object val) {
        SnmpInstId inst = m_tracker.receivedOid(oid);
        if (inst != null) {
            System.err.println("Adding result for inst "+inst+": "+val);
            m_results.put(inst, val);
        }
        return inst;
        
    }

    public Object getResultForInstance(String inst) {
       return getResultForInstance(new SnmpInstId(inst));
    }

    public Object getResultForInstance(SnmpInstId inst) {
        return m_results.get(inst);
    }
    
    public boolean isNonRepeater() {
        return m_tracker.isNonRepeater();
    }

    public void errorOccurred() {
        m_tracker.errorOccurred();
    }

    public void receivedEndOfMib() {
        m_tracker.receivedEndOFMib();
    }

}
