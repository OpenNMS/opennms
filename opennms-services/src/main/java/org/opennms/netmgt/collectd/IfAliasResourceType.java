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
//
// 2006 Aug 15: Use generics for collections.
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
import java.util.Map;

import org.opennms.netmgt.snmp.SnmpInstId;

/**
 * <p>IfAliasResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IfAliasResourceType extends ResourceType {

    private IfResourceType m_ifResourceType;
    private Map<Integer, AliasedResource> m_aliasedIfs = new HashMap<Integer, AliasedResource>();
    private ServiceParameters m_params;

    /**
     * <p>Constructor for IfAliasResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     * @param params a {@link org.opennms.netmgt.collectd.ServiceParameters} object.
     * @param ifResourceType a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     */
    public IfAliasResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, ServiceParameters params, IfResourceType ifResourceType) {
        super(agent, snmpCollection);
        m_ifResourceType = ifResourceType;
        m_params = params;
    }

    /** {@inheritDoc} */
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        // This is here for completeness but it should not get called here.
        // findAliasedResource should be called instead
        log().debug("findResource: Should not get called from IfAliasResourceType");
        return null;
    }
    /** {@inheritDoc} */
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        Integer key = inst.toInt();
        AliasedResource resource = (AliasedResource) m_aliasedIfs.get(key);
        if (resource == null) {
            IfInfo ifInfo = (IfInfo)m_ifResourceType.findResource(inst);
            
            if(ifInfo == null) {
            	log().info("Not creating an aliased resource for ifInfo = null");
            } else {
                log().info("Creating an aliased resource for "+ifInfo);
            
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
    public Collection<SnmpAttributeType> loadAttributeTypes() {
        return getCollection().getAliasAttributeTypes(getAgent());
   }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<AliasedResource> getResources() {
        return m_aliasedIfs.values();
    }
    

}
