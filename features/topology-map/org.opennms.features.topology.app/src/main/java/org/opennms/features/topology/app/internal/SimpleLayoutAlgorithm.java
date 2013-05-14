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

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.topo.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLayoutAlgorithm implements LayoutAlgorithm {
	
	private static final Logger s_log = LoggerFactory.getLogger(SimpleLayoutAlgorithm.class);

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.topology.LayoutAlgorithm#updateLayout(org.opennms.features.vaadin.topology.Graph)
     */
        @Override
    public void updateLayout(GraphContainer graphContainer) {

    	Graph g = graphContainer.getGraph();
    	
		Layout layout = g.getLayout();

        int r = 100;
        int cx = 500;
        int cy = 500;
        
		int i = 0;
		for(Vertex vertex : g.getDisplayVertices()) {
            s_log.debug("Laying out vertex id : {}", vertex);
			if(i == 0) {
				layout.setLocation(vertex, cx, cy);
            }else {
    	        int n = i - 1;
    	        double a = (2*Math.PI)/(g.getDisplayVertices().size() -1);
    	        
    	        int x = (int) (r * Math.cos(n*a) + cx);
    	        int y = (int) (r * Math.sin(n*a) + cy);

    	        layout.setLocation(vertex, x, y);
            }
			i++;
        }
    }

    
}
