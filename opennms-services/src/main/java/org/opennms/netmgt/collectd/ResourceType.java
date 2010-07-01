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

/**
 * <p>Abstract ResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class ResourceType {
    
    private CollectionAgent m_agent;
    private OnmsSnmpCollection m_snmpCollection;
    private Collection<SnmpAttributeType> m_attributeTypes;

    /**
     * <p>Constructor for ResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    public ResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        m_agent = agent;
        m_snmpCollection = snmpCollection;
    }

    /**
     * <p>getAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public CollectionAgent getAgent() {
        return m_agent;
    }
    
    /**
     * <p>getCollectionName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getCollectionName() {
        return m_snmpCollection.getName();
    }
    
    /**
     * <p>getCollection</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    protected OnmsSnmpCollection getCollection() {
        return m_snmpCollection;
    }

    /**
     * <p>getAttributeTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    final public Collection<SnmpAttributeType> getAttributeTypes() {
        if (m_attributeTypes == null) {
            m_attributeTypes = loadAttributeTypes();
        }
        return m_attributeTypes;
    }
    
    /**
     * <p>loadAttributeTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    protected abstract Collection<SnmpAttributeType> loadAttributeTypes();

    /**
     * <p>hasDataToCollect</p>
     *
     * @return a boolean.
     */
    protected boolean hasDataToCollect() {
        return !getAttributeTypes().isEmpty();
    }
    
    /**
     * This method returns an array of the instances that the attributes of this type should be collected for
     * It is used to restricting data collection to just these instances.  It is useful for collecting only the
     * required data when a small amount of data from a large table is being collected.
     *
     * @return an array of {@link org.opennms.netmgt.snmp.SnmpInstId} objects.
     */
    public SnmpInstId[] getCollectionInstances() {
        return null;
    }

    /**
     * <p>findResource</p>
     *
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @return a {@link org.opennms.netmgt.collectd.SnmpCollectionResource} object.
     */
    public abstract SnmpCollectionResource findResource(SnmpInstId inst);

    /**
     * <p>findAliasedResource</p>
     *
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @param ifAlias a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.collectd.SnmpCollectionResource} object.
     */
    public abstract SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias);
    
    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<? extends SnmpCollectionResource> getResources();
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() { return ThreadCategory.getInstance(getClass()); }
}
