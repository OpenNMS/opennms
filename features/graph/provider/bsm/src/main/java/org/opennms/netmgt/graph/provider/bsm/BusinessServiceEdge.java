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
package org.opennms.netmgt.graph.provider.bsm;

import static org.opennms.netmgt.graph.provider.bsm.BusinessServiceVertex.convert;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.graph.api.enrichment.EnrichedProperties;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.info.Severity;
import org.opennms.netmgt.graph.domain.AbstractDomainEdge;

public final class BusinessServiceEdge extends AbstractDomainEdge {

    interface Properties {
        String MAP_FUNCTION = "mapFunction";
        String WEIGHT = "weight";
    }

    public BusinessServiceEdge(GenericEdge genericEdge) {
        super(genericEdge);
    }

    public MapFunction getMapFunction() {
        return this.delegate.getProperty(Properties.MAP_FUNCTION);
    }

    public float getWeight() {
        return this.delegate.getProperty(Properties.WEIGHT);
    }

    public final static BusinessServiceEdgeBuilder builder() {
        return new BusinessServiceEdgeBuilder();
    }
    
    public static BusinessServiceEdge from(GenericEdge genericEdge) {
        return new BusinessServiceEdge(genericEdge);
    }
    
    public final static class BusinessServiceEdgeBuilder extends AbstractDomainEdgeBuilder<BusinessServiceEdgeBuilder> {
        
        private BusinessServiceEdgeBuilder() {}
        
        BusinessServiceEdgeBuilder graphEdge(GraphEdge graphEdge) {
            mapFunction(graphEdge.getMapFunction());
            weight(graphEdge.getWeight());
            status(graphEdge.getStatus());
            return this;
        }
        
        BusinessServiceEdgeBuilder weight(float weight) {
            property(Properties.WEIGHT, weight);
            return this;
        }
       
        BusinessServiceEdgeBuilder mapFunction(MapFunction mapFunction) {
            property(Properties.MAP_FUNCTION, mapFunction);
            return this;
        }

        BusinessServiceEdgeBuilder status(final Status status) {
            final Severity severity = convert(status);
            property(EnrichedProperties.STATUS, severity);
            return this;
        }
        
        public BusinessServiceEdge build() {
            return new BusinessServiceEdge(GenericEdge.builder()
                    .namespace(BusinessServiceGraph.NAMESPACE) // default but can still be changed by properties
                    .properties(properties).source(source).target(target).build());
        }
    }
}
