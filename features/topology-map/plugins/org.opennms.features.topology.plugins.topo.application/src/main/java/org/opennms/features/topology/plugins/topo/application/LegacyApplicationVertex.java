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
package org.opennms.features.topology.plugins.topo.application;

import org.opennms.features.topology.api.topo.AbstractLevelAwareVertex;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.provider.application.ApplicationVertex;
import org.opennms.netmgt.graph.provider.application.ApplicationVertexType;
import org.opennms.netmgt.model.OnmsApplication;

public class LegacyApplicationVertex extends AbstractLevelAwareVertex implements LevelAware {

    private Integer serviceTypeId;

    public LegacyApplicationVertex(org.opennms.netmgt.graph.provider.application.ApplicationVertex vertex) {
        this(vertex.getId(), vertex.getName());
        final boolean isApplication = (vertex.getVertexType() == ApplicationVertexType.Application);
        if(isApplication) {
            setTooltipText(String.format("Application '%s'", vertex.getName()));
            setIconKey("application.application");
        } else {
            setTooltipText(String.format("Service '%s', IP: %s", vertex.getName(), vertex.getIpAddress()));
            setIpAddress(vertex.getIpAddress());
            setServiceTypeId(vertex.getServiceTypeId());
            setIconKey("application.monitored-service");

            // Apply node Id if provided
            final NodeRef nodeRef = vertex.getNodeRef();
            if (nodeRef != null && nodeRef.getNodeId() != null) {
                setNodeID(nodeRef.getNodeId());
            }
        }
    }

    public LegacyApplicationVertex(OnmsApplication application) {
        this(application.getId().toString(), application.getName());
        setTooltipText(String.format("Application '%s'", application.getName()));
        setIconKey("application.application");
    }

    /**
     * Creates a new {@link ApplicationVertex}.
     * @param id the unique id of this vertex. Must be unique overall the namespace.
     */
    public LegacyApplicationVertex(String id, String label) {
        super(LegacyApplicationTopologyProvider.TOPOLOGY_NAMESPACE, id, label);
    }
    
    public void setServiceTypeId(Integer serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public Integer getServiceTypeId() {
        return serviceTypeId;
    }

    public boolean isPartOf(String applicationId) {
        return applicationId != null && applicationId.equals(getRoot().getId());
    }

    public LegacyApplicationVertex getRoot() {
        if (isRoot()) {
            return this;
        }
        return ((LegacyApplicationVertex)getParent()).getRoot();
    }

    @Override
    public int getLevel() {
        return isRoot() ? 0 : 1;
    }
}
