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


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class SnmpCollectionTracker extends AggregateTracker {
    
    public static final String COLUMN = "column";
    
    public interface CollectionDefinition {
        public String getOid();
        public String getInstanceDef();
    }
    
    private static CollectionTracker[] createCollectionTrackers(List objList) {
        CollectionTracker[] trackers = new CollectionTracker[objList.size()];
        int index = 0;
        for (Iterator it = objList.iterator(); it.hasNext();) {
            CollectionDefinition colDef = (CollectionDefinition) it.next();
            trackers[index++] = createTracker(SnmpObjId.get(colDef.getOid()), colDef.getInstanceDef());
        }
        return trackers;
    }
    /*
     * TODO: handle repition hints
     * TODO: handle hint oids to be retrieved in first packet
     * TODO: handle hint values passed in with initial data
     * 
     */

    private static CollectionTracker createTracker(SnmpObjId id, String instanceDef) {
        if (SnmpCollectionTracker.COLUMN.equals(instanceDef)) {
            return new ColumnTracker(id);
        } else {
            return new InstanceListTracker(id, instanceDef);
        }
    }
    
    private Map m_instanceMaps = new TreeMap();

    public SnmpCollectionTracker(List objList) {
        super(createCollectionTrackers(objList));
    }
    
    protected void storeResult(SnmpObjId base, SnmpInstId inst, Object val) {
        System.err.println("Storing result ["+base+"].["+inst+"] = "+val);
        super.storeResult(base, inst, val);
        Map resultsForInstance = (Map)m_instanceMaps.get(inst);
        if (resultsForInstance == null) {
            resultsForInstance = new TreeMap();
            m_instanceMaps.put(inst, resultsForInstance);
        }
        resultsForInstance.put(base, val);
    }
    
    public Map getDataForInstance(SnmpInstId id) {
        return (Map)m_instanceMaps.get(id) ;
    }

    public Set getInstances() {
        return Collections.unmodifiableSet(m_instanceMaps.keySet());
    }
    public Collection getDataForInstances() {
        return Collections.unmodifiableCollection(m_instanceMaps.values());
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }



}
