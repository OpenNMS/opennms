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

package org.opennms.features.vaadin.topology;

import static org.opennms.features.vaadin.app.TopologyWidgetTestApplication.SERVER_ICON;
import static org.opennms.features.vaadin.app.TopologyWidgetTestApplication.GROUP_ICON;
import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleGraphContainerTest {

	@Test
	public void test() {
		SimpleGraphContainer container = new SimpleGraphContainer();
		
		container.addVertex("a", 50, 100, SERVER_ICON);
		container.addVertex("b", 100, 50, SERVER_ICON);
		container.addVertex("c", 100, 150, SERVER_ICON);
		container.addVertex("d", 150, 100, SERVER_ICON);
		container.addVertex("e", 200, 200, SERVER_ICON);
		container.addGroup("g1", GROUP_ICON);
		container.addGroup("g2", GROUP_ICON);
		container.getVertexContainer().setParent("a", "g1");
		container.getVertexContainer().setParent("b", "g1");
		container.getVertexContainer().setParent("c", "g2");
		container.getVertexContainer().setParent("d", "g2");
		
		container.connectVertices("e1", "a", "b");
		container.connectVertices("e2", "a", "c");
		container.connectVertices("e3", "b", "c");
		container.connectVertices("e4", "b", "d");
		container.connectVertices("e5", "c", "d");
		container.connectVertices("e6", "a", "e");
		container.connectVertices("e7", "d", "e");
		
		container.save("test-graph.xml");
		
		container.load("test-graph.xml");
		
	}
	
	
	

}
