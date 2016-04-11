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
