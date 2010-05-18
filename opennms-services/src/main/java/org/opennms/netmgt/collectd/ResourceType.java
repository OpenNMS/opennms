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
// Modifications:
// 2006 Aug 15: Use generics for collections - dj@opennms.org
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

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;

public abstract class ResourceType {
    
    private CollectionAgent m_agent;
    private OnmsSnmpCollection m_snmpCollection;
    private Collection<SnmpAttributeType> m_attributeTypes;

    public ResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        m_agent = agent;
        m_snmpCollection = snmpCollection;
    }

    public CollectionAgent getAgent() {
        return m_agent;
    }
    
    protected String getCollectionName() {
        return m_snmpCollection.getName();
    }
    
    protected OnmsSnmpCollection getCollection() {
        return m_snmpCollection;
    }

    final public Collection<SnmpAttributeType> getAttributeTypes() {
        if (m_attributeTypes == null) {
            m_attributeTypes = loadAttributeTypes();
        }
        return m_attributeTypes;
    }
    
    protected abstract Collection<SnmpAttributeType> loadAttributeTypes();

    protected boolean hasDataToCollect() {
        return !getAttributeTypes().isEmpty();
    }
    
    /**
     * This method returns an array of the instances that the attributes of this type should be collected for
     * It is used to restricting data collection to just these instances.  It is useful for collecting only the 
     * required data when a small amount of data from a large table is being collected.
     */
    public SnmpInstId[] getCollectionInstances() {
        return null;
    }

    public abstract SnmpCollectionResource findResource(SnmpInstId inst);

    public abstract SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias);
    
    public abstract Collection<? extends SnmpCollectionResource> getResources();
    
    public ThreadCategory log() { return ThreadCategory.getInstance(getClass()); }
}
