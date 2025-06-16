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
    
    private SnmpCollectionAgent m_agent;
    private OnmsSnmpCollection m_snmpCollection;
    private Collection<SnmpAttributeType> m_attributeTypes;

    /**
     * <p>Constructor for ResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    public ResourceType(SnmpCollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        m_agent = agent;
        m_snmpCollection = snmpCollection;
    }

    /**
     * <p>getAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public SnmpCollectionAgent getAgent() {
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
    public final Collection<SnmpAttributeType> getAttributeTypes() {
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
