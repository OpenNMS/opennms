//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: Javadoc, formatting, improve an error message. - dj@opennms.org
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
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;

/**
 * Represents an OID to be collected (it might be specific or an indexed object).
 * Also specific to a collection (e.g.: "default"), resource type (e.g.: node or
 * interface), and attribute group (data collection group name, e.g.: "mib2-interfaces").
 * This is extended to create concreate classes that represent specific types of data
 * to be stored such as numeric data ({@link (NumericAttributeType)}) or string data
 * ({@link (StringAttributeType)}).
 */
public abstract class SnmpAttributeType implements AttributeDefinition,CollectionAttributeType {
    
    private MibObject m_mibObj;
    private String m_collectionName;
    private ResourceType m_resourceType;
    private AttributeGroupType m_groupType;

    protected SnmpAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
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
    public static List<Collectable> getCollectionTrackers(Collection<SnmpAttributeType> objList) {
        ArrayList<Collectable> trackers = new ArrayList<Collectable>(objList.size());
        for (SnmpAttributeType attrType : objList) {
            trackers.add(attrType.getCollectionTracker());
        }
        
        return trackers;
    }
    
    private CollectionTracker getCollectionTracker() {
        SnmpInstId[] instances = m_resourceType.getCollectionInstances();
        if (instances != null && Boolean.getBoolean("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances")) {
            return getMibObj().getCollectionTracker(instances);
        } else {
            return getMibObj().getCollectionTracker();
        }
    }
    

    public static SnmpAttributeType create(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        if (NumericAttributeType.supportsType(mibObj.getType())) {
            return new NumericAttributeType(resourceType, collectionName, mibObj, groupType);
        }
        if (StringAttributeType.supportsType(mibObj.getType())) {
            return new StringAttributeType(resourceType, collectionName, mibObj, groupType);
        }
        
        throw new IllegalArgumentException("No support exists for AttributeType '" + mibObj.getType() + "' for MIB object: "+ mibObj);
    }
    
    public ResourceType getResourceType() {
        return m_resourceType;
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AttributeDefinition#getType()
     */
    public String getType() {
        return m_mibObj.getType();
    }

    SnmpObjId getSnmpObjId() {
        return m_mibObj.getSnmpObjId();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AttributeDefinition#getName()
     */
    public String getName() {
        return getAlias();
    }

    public abstract void storeAttribute(CollectionAttribute attribute, Persister persister);
    
    public void storeResult(SnmpCollectionSet collectionSet, SNMPCollectorEntry entry, SnmpResult res) {
        log().info("Setting attribute: "+this+".["+res.getInstance()+"] = '"+res.getValue()+"'");
        SnmpCollectionResource resource = null;
        if(this.getAlias().equals("ifAlias")) {
            resource = m_resourceType.findAliasedResource(res.getInstance(), res.getValue().toString());
        } else {
            resource = m_resourceType.findResource(res.getInstance());
        }
        if (resource == null) {
            collectionSet.notifyIfNotFound(this, res);
        } else {
            resource.setAttributeValue(this, res.getValue());
        }
    }

    public String toString() {
        return getAlias()+" ["+getOid()+"]";
    }

    public boolean equals(Object obj) {
        if (obj instanceof SnmpAttributeType) {
            SnmpAttributeType attrType = (SnmpAttributeType) obj;
            return attrType.m_resourceType.equals(m_resourceType) && attrType.getAlias().equals(getAlias());
        }
        return false;
    }

    public int hashCode() {
        return getAlias().hashCode();
    }
    
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public String getGroupIfType() {
        return m_groupType.getIfType();
    }

    public boolean matches(SnmpObjId base, SnmpInstId inst) {
        if (!base.equals(getSnmpObjId())) {
        	return false;
        }
        
        if (getInstance().equals(MibObject.INSTANCE_IFINDEX) || m_mibObj.getResourceType() != null) {
            return true;
        } else { 
            return getInstance().equals(inst.toString());
        }
    }



}
