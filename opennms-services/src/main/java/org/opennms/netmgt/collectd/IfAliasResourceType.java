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
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>IfAliasResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IfAliasResourceType extends ResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(IfAliasResourceType.class);
    
    private IfResourceType m_ifResourceType;
    private Map<Integer, AliasedResource> m_aliasedIfs = new HashMap<Integer, AliasedResource>();
    private ServiceParameters m_params;

    /**
     * <p>Constructor for IfAliasResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     * @param ifResourceType a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     */
    public IfAliasResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, ServiceParameters params, IfResourceType ifResourceType) {
        super(agent, snmpCollection);
        m_ifResourceType = ifResourceType;
        m_params = params;
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        // This is here for completeness but it should not get called here.
        // findAliasedResource should be called instead
        LOG.debug("findResource: Should not get called from IfAliasResourceType");
        return null;
    }
    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        Integer key = inst.toInt();
        AliasedResource resource = (AliasedResource) m_aliasedIfs.get(key);
        if (resource == null) {
            IfInfo ifInfo = (IfInfo)m_ifResourceType.findResource(inst);
            
            if(ifInfo == null) {
            	LOG.info("Not creating an aliased resource for ifInfo = null");
            } else {
                LOG.info("Creating an aliased resource for {}", ifInfo);
            
                resource = new AliasedResource(this, m_params.getDomain(), ifInfo, m_params.getIfAliasComment(), ifAlias);
            
                m_aliasedIfs.put(key, resource);
            }
        }
        return resource;
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpInstId[] getCollectionInstances() {
        return m_ifResourceType.getCollectionInstances();
    }

    /**
     * <p>loadAttributeTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<SnmpAttributeType> loadAttributeTypes() {
        return getCollection().getAliasAttributeTypes(getAgent());
   }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<AliasedResource> getResources() {
        return m_aliasedIfs.values();
    }

    //TODO Tak cleanup toString super hack
    @Override
    public String toString() {
        return super.toString() +  " IfAliasResourceType{" + "m_ifResourceType=" + m_ifResourceType + ", m_aliasedIfs=" + m_aliasedIfs + ", m_params=" + m_params + '}';
    }
}
