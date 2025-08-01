/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.collection.api.ServiceParameters;
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
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     * @param params a {@link org.opennms.netmgt.collection.api.ServiceParameters} object.
     * @param ifResourceType a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     */
    public IfAliasResourceType(SnmpCollectionAgent agent, OnmsSnmpCollection snmpCollection, ServiceParameters params, IfResourceType ifResourceType) {
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
