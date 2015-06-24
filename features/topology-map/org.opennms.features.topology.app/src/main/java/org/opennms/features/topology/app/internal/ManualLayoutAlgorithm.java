/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;

import java.util.Collection;

public class ManualLayoutAlgorithm implements LayoutAlgorithm {

        @Override
	public void updateLayout(GraphContainer graphContainer) {
        Collection<Vertex> vertices = graphContainer.getGraph().getDisplayVertices();
        Layout layout = graphContainer.getGraph().getLayout();

        for (Vertex vertex : vertices) {
            Point p = layout.getLocation(vertex);
            layout.setLocation(vertex, p.getX(), p.getY());
        }

	}

}
