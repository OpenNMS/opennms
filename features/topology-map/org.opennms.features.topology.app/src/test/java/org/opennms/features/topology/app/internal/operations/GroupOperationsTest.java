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

package org.opennms.features.topology.app.internal.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphProvider;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;

public class GroupOperationsTest {

	private static class TestOperationContext implements OperationContext {

		private final GraphContainer m_graphContainer;
		private final Window m_window;

		public TestOperationContext(GraphContainer graphContainer) {
			m_graphContainer = graphContainer;
			m_window = new Window();
		}

		@Override
		public Window getMainWindow() {
			return m_window;
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

	private static TestOperationContext getOperationContext(GraphContainer mockedContainer) {
		return new TestOperationContext(mockedContainer);
	}

	private SimpleGraphProvider m_topologyProvider;

	@Before
	public void setUp() throws MalformedURLException, JAXBException {
		if(m_topologyProvider == null) {
			m_topologyProvider = new SimpleGraphProvider();
			m_topologyProvider.setTopologyLocation(new File("target/test-classes/graph.xml").toURI());
		}

		m_topologyProvider.resetContainer();

		MockLogAppender.setupLogging();
	}

	@After
	public void tearDown() {
		if(m_topologyProvider != null) {
			m_topologyProvider.resetContainer();
		}
	}

	@Test
	public void testAddVertexToGroupOperation() {

		m_topologyProvider.resetContainer();

		Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
		Vertex group1 = m_topologyProvider.addGroup("NEW GROUP", null);

		GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

		EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
		graphContainer.redoLayout();
		graphContainer.redoLayout();
		graphContainer.redoLayout();

		EasyMock.replay(graphContainer);

		Collection<Vertex> vertices = m_topologyProvider.getVertices();
		assertEquals(2, vertices.size());
		assertEquals(0, m_topologyProvider.getChildren(group1).size());

		{
			AddVertexToGroupOperation operation = new AddVertexToGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			// Execute the operation on the single vertex
			operation.execute(Collections.singletonList((VertexRef)vertex1), context);

			// Even though we have executed the operation, it is waiting on a Window
			// operation to commit the change so make sure the vertex hasn't been
			// added yet.
			vertices = m_topologyProvider.getVertices();
			assertEquals(2, vertices.size());
			assertEquals(0, m_topologyProvider.getChildren(group1).size());

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertex1));

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field group = form.getField("Group");
					group.setValue(group1.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(group1.getId(), group.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			vertices = m_topologyProvider.getVertices();
			assertEquals(2, vertices.size());

			assertEquals(1, m_topologyProvider.getChildren(group1).size());

			// Verify that the semantic zoom level of the vertices is correct
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
		}

		// Add a second group so that we can make sure that adding groups to other groups works also
		Vertex group2 = m_topologyProvider.addGroup("Another new group", null);
		vertices = m_topologyProvider.getVertices();
		assertEquals(3, vertices.size());
		assertEquals(1, m_topologyProvider.getChildren(group1).size());
		assertEquals(0, m_topologyProvider.getChildren(group2).size());

		assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
		assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group2));
		assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));

		{
			AddVertexToGroupOperation operation = new AddVertexToGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			// Execute the operation on the single vertex
			operation.execute(Collections.singletonList((VertexRef)group2), context);

			// Even though we have executed the operation, it is waiting on a Window
			// operation to commit the change so make sure the vertex hasn't been
			// added yet.
			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());
			assertEquals(1, m_topologyProvider.getChildren(group1).size());
			assertEquals(0, m_topologyProvider.getChildren(group2).size());

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field group = form.getField("Group");
					group.setValue(group1.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(group1.getId(), group.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());

			// Verify that the semantic zoom level of the vertices is correct
			assertEquals(2, m_topologyProvider.getChildren(group1).size());
			assertEquals(0, m_topologyProvider.getChildren(group2).size());

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
		}

		// Now let's move the vertex down to szl 2
		{
			AddVertexToGroupOperation operation = new AddVertexToGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			// Execute the operation on the single vertex
			operation.execute(Collections.singletonList((VertexRef)vertex1), context);

			// Even though we have executed the operation, it is waiting on a Window
			// operation to commit the change so make sure the vertex hasn't been
			// added yet.
			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());
			assertEquals(2, m_topologyProvider.getChildren(group1).size());
			assertEquals(0, m_topologyProvider.getChildren(group2).size());

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field group = form.getField("Group");
					group.setValue(group2.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(group2.getId(), group.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());

			assertEquals(1, m_topologyProvider.getChildren(group1).size());
			assertEquals(1, m_topologyProvider.getChildren(group2).size());

			// Verify that the semantic zoom level of the vertices is correct
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(2, m_topologyProvider.getSemanticZoomLevel(vertex1));
		}

		EasyMock.verify(graphContainer);
	}

	@Test
	public void testCreateGroupOperation() {
		m_topologyProvider.resetContainer();

		Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
		Vertex vertex2 = m_topologyProvider.addVertex(0, 0);

		GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

		EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
		graphContainer.redoLayout();

		EasyMock.replay(graphContainer);

		CreateGroupOperation groupOperation = new CreateGroupOperation();
		OperationContext context = getOperationContext(graphContainer);
		groupOperation.execute(Arrays.asList((VertexRef)vertex1, vertex2), context);

		assertNull("Value should be null: " + vertex1.getParent(), vertex1.getParent());

		// Grab the window, put a value into the form field, and commit the form to complete
		// the operation.
		Window window = context.getMainWindow();
		assertEquals(1, window.getChildWindows().size());
		Window prompt = window.getChildWindows().iterator().next();

		for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
			Component component = itr.next();
			try {
				Form form = (Form)component;
				Field group = form.getField("Group Label");
				group.setValue("My New Awesome Group");
				// Make sure that the value was set
				assertEquals("My New Awesome Group", group.getValue());
				form.commit();
			} catch (ClassCastException e) {
				LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
			}
		}

		// Refresh the contents of the vertex
		vertex1 = m_topologyProvider.getVertex(vertex1);
		VertexRef parent = vertex1.getParent();
		assertNotNull("Value should not be null: " + parent, parent);
		assertEquals("My New Awesome Group", m_topologyProvider.getVertex(parent).getLabel());
		assertEquals(2, m_topologyProvider.getChildren(parent).size());

		EasyMock.verify(graphContainer);
	}
}
