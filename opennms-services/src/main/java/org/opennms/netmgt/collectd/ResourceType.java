/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract ResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class ResourceType {
    
    public static final Logger LOG = LoggerFactory.getLogger(ResourceType.class);
    
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
}
