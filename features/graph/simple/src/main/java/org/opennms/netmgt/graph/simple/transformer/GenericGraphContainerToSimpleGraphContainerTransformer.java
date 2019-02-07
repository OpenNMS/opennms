/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.simple.transformer;

import java.util.Objects;
import java.util.function.Function;

import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleGraphContainer;

public class GenericGraphContainerToSimpleGraphContainerTransformer implements Function<GenericGraphContainer, SimpleGraphContainer> {
    @Override
    public SimpleGraphContainer apply(GenericGraphContainer genericGraphContainer) {
        Objects.requireNonNull(genericGraphContainer);
        final SimpleGraphContainer simpleGraphContainer = new SimpleGraphContainer(genericGraphContainer.getId());
        simpleGraphContainer.setLabel(genericGraphContainer.getLabel());
        simpleGraphContainer.setDescription(genericGraphContainer.getDescription());
        genericGraphContainer.getGraphs().forEach(genericGrah -> {
            final SimpleGraph simpleGraph = new GenericGraphToSimpleGraphTransformer().apply(genericGrah); // TODO MVR this should not be necessary
            simpleGraphContainer.addGraph(simpleGraph);
        });
        return simpleGraphContainer;
    }
}
