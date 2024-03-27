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
package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLayoutAlgorithm implements LayoutAlgorithm {
	
	private static final Logger s_log = LoggerFactory.getLogger(SimpleLayoutAlgorithm.class);

	@Override
    public void updateLayout(Graph graph) {
		Layout layout = graph.getLayout();

        int r = 100;
        int cx = 500;
        int cy = 500;
        
		int i = 0;
		for(Vertex vertex : graph.getDisplayVertices()) {
            s_log.debug("Laying out vertex id : {}", vertex);
			if(i == 0) {
				layout.setLocation(vertex, new Point(cx, cy));
            } else {
    	        int n = i - 1;
    	        double a = (2*Math.PI)/(graph.getDisplayVertices().size() -1);
    	        
    	        int x = (int) (r * Math.cos(n*a) + cx);
    	        int y = (int) (r * Math.sin(n*a) + cy);

    	        layout.setLocation(vertex, new Point(x, y));
            }
			i++;
        }
    }

    
}
