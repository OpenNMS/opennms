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

package org.opennms.features.topology.plugins.devutils.internal;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphProvider;

import com.vaadin.data.Property;
import com.vaadin.ui.Window;

public class SaveToXmlTest {

	private class TestOperationContext implements OperationContext{

		private GraphContainer m_graphContainer;

		public TestOperationContext(GraphContainer graphContainer) {
			m_graphContainer = graphContainer;
		}

		@Override
		public Window getMainWindow() {
			return EasyMock.createMock(Window.class);
		}

		@Override
		public GraphContainer getGraphContainer() {
			return m_graphContainer;
		}

		@Override
		public boolean isChecked() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public DisplayLocation getDisplayLocation() {
			return DisplayLocation.MENUBAR;
		}

	}

	@Test
	public void testSave() throws Exception {

		final SimpleGraphProvider simpleTopo = new SimpleGraphProvider();
		simpleTopo.load("test-graph.xml");

		SaveToXmlOperation saver = new SaveToXmlOperation();

		GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);
		EasyMock.expect(graphContainer.getBaseTopology()).andReturn(simpleTopo).anyTimes();
		EasyMock.replay(graphContainer);

		saver.execute(null, new TestOperationContext(graphContainer));
	}

    public Property getScaleProperty() {
        return null;
    }
}
