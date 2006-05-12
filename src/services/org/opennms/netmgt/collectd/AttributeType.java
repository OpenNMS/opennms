//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class AttributeType {
    
    public boolean equals(Object obj) {
        if (obj instanceof AttributeType) {
            AttributeType attrType = (AttributeType) obj;
            return attrType.m_resourceType.equals(m_resourceType) && attrType.getAlias().equals(getAlias());
        }
        return false;
    }

    public int hashCode() {
        return m_mibObj.hashCode();
    }

    private MibObject m_mibObj;
    private DataSource m_ds = null;
    private String m_collectionName;
    private ResourceType m_resourceType;

    public AttributeType(ResourceType resourceType, String collectionName, MibObject mibObj) {
        m_resourceType = resourceType;
        m_collectionName = collectionName;
        m_mibObj = mibObj;
    }

    public MibObject getMibObj() {
        return m_mibObj;
    }

    public DataSource getDs() {
        if (m_ds == null) {
            m_ds = DataSource.dataSourceForMibObject(m_mibObj, m_collectionName);
        }
        return m_ds;
    }

    // FIXME: CollectionAttribute should be a tracker of its own
    // Also these should be created directly by the DAO rather 
    // than MibObject.
    public static List getCollectionTrackers(Collection objList) {
        ArrayList trackers = new ArrayList(objList.size());
        for (Iterator iter = objList.iterator(); iter.hasNext();) {
            AttributeType attrType = (AttributeType) iter.next();
            trackers.add(attrType.getMibObj().getCollectionTracker());
        }
        
        return trackers;
    }

    public String getAlias() {
        return m_mibObj.getAlias();
    }
    
    public String getOid() {
        return m_mibObj.getOid();
    }

    public String getInstance() {
        return m_mibObj.getInstance();
    }

    public String getType() {
        return m_mibObj.getType();
    }

    SnmpObjId getSnmpObjId() {
        return getMibObj().getSnmpObjId();
    }

    public String getName() {
        return getAlias();
    }

    String getValue(SNMPCollectorEntry entry) {
        return getDs().getRRDValue(entry);
    }

    boolean performUpdate(CollectionAgent collectionAgent, File resourceDir, SNMPCollectorEntry entry) {
        return getDs().performUpdate(collectionAgent.getHostAddress(), resourceDir, getValue(entry));
    }
    
    public void storeResult(SNMPCollectorEntry entry, SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        CollectionResource resource = m_resourceType.findResource(inst);
        resource.setEntry(entry);
        if (resource == null) {
            logNoSuchResource(base, inst, val);
            return;
        }
        resource.setAttributeValue(this, val);
    }

    private void logNoSuchResource(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        log().info("Unable to locate resource with instance id "+inst+" while collecting attribute "+this);
    }
    
    public String toString() {
        return getAlias()+" ["+getOid()+"]";
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
