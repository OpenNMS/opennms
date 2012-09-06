/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;

public class SimpleLayoutAlgorithm implements LayoutAlgorithm {

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.topology.LayoutAlgorithm#updateLayout(org.opennms.features.vaadin.topology.Graph)
     */
    public void updateLayout(GraphContainer graphContainer) {
    	int szl = graphContainer.getSemanticZoomLevel();
    	Graph graph = new Graph(graphContainer);
        int r = 100;
        int cx = 500;
        int cy = 500;
        List<Vertex> vertices = graph.getVertices(szl);
		for(int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
			System.err.println("Laying out Vertex " + vertex);
            if(i == 0) {
                vertex.setX(cx);
                vertex.setY(cy);
            }else {
    	        int n = i - 1;
    	        double a = (2*Math.PI)/(vertices.size() -1);
    	        
    	        int x = (int) (r * Math.cos(n*a) + cx);
    	        int y = (int) (r * Math.sin(n*a) + cy);
    	        
    	        vertex.setX(x);
    	        vertex.setY(y);
            }
        }
    }
    
}
