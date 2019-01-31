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

package org.opennms.features.graph.providers.bsm;

import org.opennms.features.graph.api.generic.GenericEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennsm.features.graphs.simple.SimpleEdge;

public class BusinessServiceEdge extends SimpleEdge {

    private final MapFunction mapFunction;
    private final float weight;

    public BusinessServiceEdge(GraphEdge graphEdge, AbstractBusinessServiceVertex source, AbstractBusinessServiceVertex target) {
        super(source, target);
        this.mapFunction = graphEdge.getMapFunction();
        this.weight = graphEdge.getWeight();
        // TODO MVR ToolTips are not yet supported
//        setTooltipText(String.format("Map function: %s, Weight: %s", graphEdge.getMapFunction().getClass().getSimpleName(), graphEdge.getWeight()));
    }

    private BusinessServiceEdge(BusinessServiceEdge edgeToClone) {
        super(edgeToClone);
        mapFunction = edgeToClone.mapFunction;
        weight = edgeToClone.weight;
    }

    public AbstractBusinessServiceVertex getBusinessServiceSource() {
        return (AbstractBusinessServiceVertex) getSource();
    }

    public AbstractBusinessServiceVertex getBusinessServiceTarget() {
        return (AbstractBusinessServiceVertex) getTarget();
    }

    public MapFunction getMapFunction() {
        return mapFunction;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public GenericEdge asGenericEdge() {
        final GenericEdge genericEdge = super.asGenericEdge();
        genericEdge.setProperty("weight", getWeight());
        genericEdge.setProperty("mapFunction", MapFunction.class.getSimpleName()); // TODO MVR ???
        return genericEdge;
    }
}
