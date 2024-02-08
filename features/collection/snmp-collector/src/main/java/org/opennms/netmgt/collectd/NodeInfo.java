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

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourcePath;

/**
 * This class encapsulates all of the node-level data required by the SNMP data
 * collector in order to successfully perform data collection for a scheduled
 * primary SNMP interface.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class NodeInfo extends SnmpCollectionResource {

	private SNMPCollectorEntry m_entry;
    private final int m_nodeId;
    private final CollectionAgent m_agent;

    /**
     * <p>Constructor for NodeInfo.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.NodeResourceType} object.
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public NodeInfo(NodeResourceType def, CollectionAgent agent) {
        super(def);
        m_agent = agent;
        m_nodeId = agent.getNodeId();
    }
    
     /**
      * <p>getType</p>
      *
      * @return a int.
      */
        @Override
     public int getSnmpIfType() {
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public ResourcePath getPath() {
        return getCollectionAgent().getStorageResourcePath();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "node["+m_nodeId+']';
    }

    /**
     * <p>setEntry</p>
     *
     * @param nodeEntry a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    public void setEntry(SNMPCollectorEntry nodeEntry) {
        m_entry = nodeEntry;
    }
    
    /**
     * <p>getEntry</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    protected SNMPCollectorEntry getEntry() {
        return m_entry;
    }

    /** {@inheritDoc} */
        @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }
    
    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_NODE; //This is a nodeInfo; must be a node type resource
    }
    
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getInstance() {
        return null; //For node type resources, use the default instance
    }

    /**
     * <p>getUnmodifiedInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getUnmodifiedInstance() {
        return getInstance();
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getInterfaceLabel() {
        return null;
    }

        @Override
    public ResourcePath getParent() {
        return m_agent.getStorageResourcePath();
    }

    public int getNodeId(){
        return m_nodeId;
    }

} // end class
