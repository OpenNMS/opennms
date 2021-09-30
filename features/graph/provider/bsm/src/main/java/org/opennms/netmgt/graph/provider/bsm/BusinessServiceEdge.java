/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
