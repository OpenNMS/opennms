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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>IfResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class IfResourceType extends ResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(IfResourceType.class);
    
    private Map<Integer, IfInfo> m_ifMap;

    /**
     * <p>Constructor for IfResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    public IfResourceType(SnmpCollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
        m_ifMap = new TreeMap<Integer, IfInfo>();
        addKnownIfResources();
    }
    
    private Map<Integer, IfInfo> getIfMap() {
        return m_ifMap;
    }

    private void addIfInfo(final IfInfo ifInfo) {
        getIfMap().put(ifInfo.getIndex(), ifInfo);
    }

    private void addKnownIfResources() {
    	Set<IfInfo> ifInfos = getAgent().getSnmpInterfaceInfo(this);
        
        for(IfInfo ifInfo : ifInfos) {
            addIfInfo(ifInfo);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpInstId[] getCollectionInstances() {
        List<SnmpInstId> instances = new ArrayList<>();
        for (IfInfo ifInfo : m_ifMap.values()) {
            if (ifInfo.isCollectionEnabled()) {
                instances.add(new SnmpInstId(ifInfo.getIndex()));
            }
        }
        return instances.toArray(new SnmpInstId[instances.size()]);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        return getIfMap().get(inst.toInt());
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        // This is here for completeness but it should not get called from here.
        // findResource should be called instead
        LOG.debug("findAliasedResource: Should not get called from IfResourceType");
        return null;
    }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<IfInfo> getResources() {
        return Collections.unmodifiableCollection(m_ifMap.values());
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<SnmpAttributeType> loadAttributeTypes() {
        return Collections.unmodifiableCollection(getCollection().getIndexedAttributeTypesForResourceType(getAgent(), this));
    }
    
    
}
