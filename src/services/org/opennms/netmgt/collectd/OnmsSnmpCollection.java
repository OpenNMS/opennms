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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

public class OnmsSnmpCollection {
    
    private ServiceParameters m_params;
    private NodeResourceType m_nodeResourceType;
    private IfResourceType m_ifResourceType;
    private IfAliasResourceType m_ifAliasResourceType;
    
    public OnmsSnmpCollection(ServiceParameters params) {
        m_params = params;
    }

    public String getName() {
        return m_params.getCollectionName();
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    int getMaxVarsPerPdu() {
    	// Retrieve configured value for max number of vars per PDU
    	int maxVarsPerPdu = DataCollectionConfigFactory.getInstance().getMaxVarsPerPdu(getName());
    	if (maxVarsPerPdu == -1) {
            Category log = log();
            if (log.isEnabledFor(Priority.WARN)) {
    			log.warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve max vars per pdu from collection: "
    							+ getName());
    		}
    		maxVarsPerPdu = SnmpCollector.DEFAULT_MAX_VARS_PER_PDU;
    	} 
        return maxVarsPerPdu;
    }

    public String getStorageFlag() {
        String collectionName = getName();
    	String storageFlag = DataCollectionConfigFactory.getInstance().getSnmpStorageFlag(collectionName);
    	if (storageFlag == null) {
            Category log = log();
            if (log.isEnabledFor(Priority.WARN)) {
    			log.warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve SNMP storage flag for collection: "
    							+ collectionName);
    		}
    		storageFlag = SnmpCollector.SNMP_STORAGE_PRIMARY;
    	}
    	return storageFlag;
    }
    
    public String toString() {
        return getName();
    }

    public CollectionSet createCollectionSet(CollectionAgent agent) {
        return new CollectionSet(agent, this);
    }
    

    public Collection getAttributeTypes(CollectionAgent agent, int ifType) {
        String sysObjectId = agent.getSysObjectId();
        String hostAddress = agent.getHostAddress();
        List oidList = DataCollectionConfigFactory.getInstance().getMibObjectList(getName(), sysObjectId, hostAddress, ifType);
        
        Map groupTypes = new HashMap();
        
        List typeList = new LinkedList();
        for (Iterator it = oidList.iterator(); it.hasNext();) {
            MibObject mibObject = (MibObject) it.next();
            String instanceName = mibObject.getInstance();
            AttributeGroupType groupType = getGroup(groupTypes, mibObject);
            AttributeType attrType = AttributeType.create(getResourceType(agent, instanceName), getName(), mibObject, groupType);
            groupType.addAttributeType(attrType);
            typeList.add(attrType);
        }
        return typeList;
    }

    private AttributeGroupType getGroup(Map groupTypes, MibObject mibObject) {
        AttributeGroupType groupType = (AttributeGroupType) groupTypes.get(mibObject.getGroupName());
        if (groupType == null) {
            groupType = new AttributeGroupType(mibObject.getGroupName(), mibObject.getGroupIfType());
            groupTypes.put(mibObject.getGroupName(), groupType);
        }
        return groupType;
    }

    public ResourceType getResourceType(CollectionAgent agent, String instanceName) {
        
        if ("ifIndex".equals(instanceName)) {
            return getIfResourceType(agent);
        } else {
            return getNodeResourceType(agent);
        }
    }

    public NodeResourceType getNodeResourceType(CollectionAgent agent) {
        if (m_nodeResourceType == null)
            m_nodeResourceType = new NodeResourceType(agent, this);
        return m_nodeResourceType;
    }

    public IfResourceType getIfResourceType(CollectionAgent agent) {
        if (m_ifResourceType == null) {
            m_ifResourceType = new IfResourceType(agent, this);
        }
        return m_ifResourceType;
    }
    
    public IfAliasResourceType getIfAliasResourceType(CollectionAgent agent) {
        if (m_ifAliasResourceType == null) {
            m_ifAliasResourceType = new IfAliasResourceType(agent, this, m_params, getIfResourceType(agent));            
        }
        return m_ifAliasResourceType;
        
    }
    
    private Collection getResourceTypes(CollectionAgent agent) {
        HashSet set = new HashSet(3);
        set.add(getNodeResourceType(agent));
        set.add(getIfResourceType(agent));
        set.add(getIfAliasResourceType(agent));
        return set;
    }
    
    public Collection getAttributeTypes(CollectionAgent agent) {
        HashSet set = new HashSet();
        for (Iterator it = getResourceTypes(agent).iterator(); it.hasNext();) {
            ResourceType resourceType = (ResourceType) it.next();
            set.addAll(resourceType.getAttributeTypes());
        }
        return set;
        
    }

    public Collection getResources(CollectionAgent agent) {
        LinkedList resources = new LinkedList();
        for (Iterator it = getResourceTypes(agent).iterator(); it.hasNext();) {
            ResourceType resourceType = (ResourceType) it.next();
            resources.addAll(resourceType.getResources());
        }
        return resources;
    }

    CollectionType getMinimumCollectionType() {
        if (getStorageFlag().equals(SnmpCollector.SNMP_STORAGE_PRIMARY))
            return CollectionType.PRIMARY;
        if (getStorageFlag().equals(SnmpCollector.SNMP_STORAGE_SELECT))
            return CollectionType.COLLECT;
        
        return CollectionType.NO_COLLECT;
    }
    

}
