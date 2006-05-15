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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public abstract class AttributeType {
    
    private MibObject m_mibObj;
    private String m_collectionName;
    private ResourceType m_resourceType;
    private AttributeGroupType m_groupType;

    protected AttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        m_resourceType = resourceType;
        m_collectionName = collectionName;
        m_mibObj = mibObj;
        m_groupType = groupType;
    }

    private MibObject getMibObj() {
        return m_mibObj;
    }
    
    protected String getCollectionName() { return m_collectionName; }

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

    public static AttributeType create(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        if (NumericAttributeType.supportsType(mibObj.getType()))
            return new NumericAttributeType(resourceType, collectionName, mibObj, groupType);
        if (StringAttributeType.supportsType(mibObj.getType()))
            return new StringAttributeType(resourceType, collectionName, mibObj, groupType);
        
        throw new IllegalArgumentException("Unable to create attribute type from "+mibObj);
    }
    
    public AttributeGroupType getGroupType() {
        return m_groupType;        
    }
    
    public String getGroupName() {
        return m_groupType.getName();
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
        return m_mibObj.getSnmpObjId();
    }

    public String getName() {
        return getAlias();
    }

    protected abstract void storeAttribute(Attribute attribute, Persister persister);
    
    public void storeResult(CollectionSet collectionSet, SNMPCollectorEntry entry, SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        log().debug("Setting attribute: "+this+".["+inst+"] = '"+val+"'");
        CollectionResource resource = m_resourceType.findResource(inst);
        if (resource == null) {
            collectionSet.notifyIfNotFound(base, inst, val);
        } else {
            resource.setAttributeValue(this, val);
        }
    }

    public String toString() {
        return getAlias()+" ["+getOid()+"]";
    }

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
    
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public String getGroupIfType() {
        return m_groupType.getIfType();
    }



}
