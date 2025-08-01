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
import java.util.Collections;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NodeResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NodeResourceType extends ResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(NodeResourceType.class);
    
    private NodeInfo m_nodeInfo;

    /**
     * <p>Constructor for NodeResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    public NodeResourceType(SnmpCollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
        m_nodeInfo = new NodeInfo(this, agent);
    }

    /**
     * <p>getNodeInfo</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.NodeInfo} object.
     */
    public NodeInfo getNodeInfo() {
        return m_nodeInfo;
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        return m_nodeInfo;
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
    // This is here for completeness but it should not get called from here.
    // findResource should be called instead
        LOG.debug("findAliasedResource: Should not get called from NodeResourceType");
        return null;
    }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<NodeInfo> getResources() {
        return Collections.singleton(m_nodeInfo);
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<SnmpAttributeType> loadAttributeTypes() {
        return getCollection().getNodeAttributeTypes(getAgent());
    }

}
