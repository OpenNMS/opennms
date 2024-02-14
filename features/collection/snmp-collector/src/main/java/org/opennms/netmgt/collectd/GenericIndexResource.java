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

import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.SnmpInstId;

/**
 * <p>GenericIndexResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GenericIndexResource extends SnmpCollectionResource {

    private final SnmpInstId m_inst;
    private final String m_name;
    private String m_resourceLabel;

    /**
     * <p>Constructor for GenericIndexResource.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param name a {@link java.lang.String} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
    public GenericIndexResource(ResourceType def, String name, SnmpInstId inst) {
        super(def);
        m_name = name;
        m_inst = inst;
    }

    @Override
    public ResourcePath getPath() {
        return getStrategy().getRelativePathForAttribute(getParent(), getInterfaceLabel());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    // NMS-5062: Avoid call getLabel here, otherwise the SiblingColumnStorageStrategy will fail if DEBUG is enabled for Collectd.
    @Override
    public String toString() {
        return "node["+getCollectionAgent().getNodeId() + "]." + getResourceTypeName() + "[" + getInstance() + "]";
    }


    /** {@inheritDoc} */
    @Override
    public int getSnmpIfType() {
        return -1;	// XXX is this right?
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return ((GenericIndexResourceType)getResourceType()).getPersistenceSelectorStrategy().shouldPersist(this);
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return m_name;
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return m_inst.toString();
    }

    @Override
    public String getUnmodifiedInstance() {
        return getInstance();
    }

    private StorageStrategy getStrategy() {
        return ((GenericIndexResourceType)getResourceType()).getStorageStrategy();
    }

    @Override
    public ResourcePath getParent() {
        return getCollectionAgent().getStorageResourcePath();
    }

    /*
     * Because call getResourceNameFromIndex could be expensive.
     * This class save the returned value from Strategy on a local variable.
     */
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        if (m_resourceLabel == null) {
            m_resourceLabel = getStrategy().getResourceNameFromIndex(this);
        }
        return m_resourceLabel;
    }
}

