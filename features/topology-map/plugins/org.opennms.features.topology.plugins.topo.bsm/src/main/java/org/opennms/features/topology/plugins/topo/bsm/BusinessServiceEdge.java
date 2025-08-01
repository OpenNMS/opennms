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

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;

public class BusinessServiceEdge extends AbstractEdge {

    private final AbstractBusinessServiceVertex source;
    private final AbstractBusinessServiceVertex target;
    private final MapFunction mapFunction;
    private final float weight;

    public BusinessServiceEdge(GraphEdge graphEdge, AbstractBusinessServiceVertex source, AbstractBusinessServiceVertex target) {
        super(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE, String.format("connection:%s:%s", source.getId(), target.getId()), source, target);
        this.source = source;
        this.target = target;
        this.mapFunction = graphEdge.getMapFunction();
        this.weight = graphEdge.getWeight();
        setTooltipText(String.format("Map function: %s, Weight: %s", graphEdge.getMapFunction().getClass().getSimpleName(), graphEdge.getWeight()));
    }

    private BusinessServiceEdge(BusinessServiceEdge edgeToClone) {
        super(edgeToClone);
        source = edgeToClone.source;
        target = edgeToClone.target;
        mapFunction = edgeToClone.mapFunction;
        weight = edgeToClone.weight;
    }

    @Override
    public AbstractEdge clone() {
        return new BusinessServiceEdge(this);
    }

    public AbstractBusinessServiceVertex getBusinessServiceSource() {
        return source;
    }

    public AbstractBusinessServiceVertex getBusinessServiceTarget() {
        return target;
    }

    public MapFunction getMapFunction() {
        return mapFunction;
    }

    public float getWeight() {
        return weight;
    }
}
