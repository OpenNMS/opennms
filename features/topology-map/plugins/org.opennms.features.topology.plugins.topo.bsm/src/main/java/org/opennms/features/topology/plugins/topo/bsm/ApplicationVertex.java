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
package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class ApplicationVertex extends AbstractBusinessServiceVertex {

    private final Integer applicationId;
    private final Set<String> reductionKeys;

    public ApplicationVertex(Application application, int level) {
        this(application.getId(),
                application.getApplicationName(),
                application.getReductionKeys(),
                level);
    }

    public ApplicationVertex(GraphVertex graphVertex) {
        this(graphVertex.getApplication(), graphVertex.getLevel());
    }

    private ApplicationVertex(int applicationId, String applicationName, Set<String> reductionKeys, int level) {
        super(Type.Application + ":" + applicationId, applicationName, level);
        this.applicationId = applicationId;
        this.reductionKeys = reductionKeys;
        setTooltipText(String.format("Application '%s'", applicationName));
        setIconKey("bsm.application");
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    @Override
    public Type getType() {
        return Type.Application;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    @Override
    public <T> T accept(BusinessServiceVertexVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
