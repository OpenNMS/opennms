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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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

import com.vaadin.data.Validator.InvalidValueException;
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
	public void testABunchOfGroupOperations() {

		m_topologyProvider.resetContainer();

		Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
		Vertex group1 = m_topologyProvider.addGroup("NEW GROUP", null);

		GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

		EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
		graphContainer.redoLayout();
		EasyMock.expectLastCall().anyTimes();

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
					Field field = form.getField("Group");
					field.setValue(group1.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(group1.getId(), field.getValue());
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
					Field field = form.getField("Group");
					field.setValue(group1.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(group1.getId(), field.getValue());
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

		try {
			renameGroup(graphContainer, group2, "");
			fail("No exception thrown");
		} catch (InvalidValueException e) {
			// This should happen since a blank label is invalid
		}

		assertEquals("Another new group", group2.getLabel());

		try {
			renameGroup(graphContainer, group2, group1.getLabel());
			fail("No exception thrown");
		} catch (InvalidValueException e) {
			// This should happen since a group with the same label already exists
		}

		assertEquals("Another new group", group2.getLabel());

		try {
			renameGroup(graphContainer, group2, null);
			fail("No exception thrown");
		} catch (InvalidValueException e) {
			// This should happen since the label cannot be null
		}

		assertEquals("Another new group", group2.getLabel());

		renameGroup(graphContainer, group2, "Valid value");
		assertEquals("Valid value", group2.getLabel());
		renameGroup(graphContainer, group2, "Another new group");
		assertEquals("Another new group", group2.getLabel());

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
					Field field = form.getField("Group");
					field.setValue(group2.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(group2.getId(), field.getValue());
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

		// Now move it back to szl 1 by removing it from its parent group
		{
			RemoveVertexFromGroupOperation operation = new RemoveVertexFromGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			// Execute the operation on the single vertex
			operation.execute(Collections.singletonList((VertexRef)group2), context);

			// Even though we have executed the operation, it is waiting on a Window
			// operation to commit the change so make sure the vertex hasn't been
			// added yet.
			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());
			assertEquals(1, m_topologyProvider.getChildren(group1).size());
			assertEquals(1, m_topologyProvider.getChildren(group2).size());

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(2, m_topologyProvider.getSemanticZoomLevel(vertex1));

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field field = form.getField("Item");
					field.setValue(vertex1.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(vertex1.getId(), field.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());

			assertEquals(2, m_topologyProvider.getChildren(group1).size());
			assertEquals(0, m_topologyProvider.getChildren(group2).size());

			// Verify that the semantic zoom level of the vertices is correct
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals("NEW GROUP", m_topologyProvider.getVertex(vertex1.getParent()).getLabel());
			assertEquals("NEW GROUP", m_topologyProvider.getParent(vertex1).getLabel());
		}

		// Now move it back to szl 1 by removing it from its parent group
		{
			RemoveVertexFromGroupOperation operation = new RemoveVertexFromGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			// Execute the operation on the single vertex
			operation.execute(Collections.singletonList((VertexRef)group1), context);

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
					Field field = form.getField("Item");
					field.setValue(vertex1.getId());
					// Make sure that the value was set, Vaadin will ignore the value
					// if, for instance, the specified value is not in the Select list
					assertEquals(vertex1.getId(), field.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			vertices = m_topologyProvider.getVertices();
			assertEquals(3, vertices.size());

			assertEquals(1, m_topologyProvider.getChildren(group1).size());
			assertEquals(0, m_topologyProvider.getChildren(group2).size());

			// Verify that the semantic zoom level of the vertices is correct
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group2));
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertex1));

			// Make sure that the vertex is back at the top level of the hierarchy
			assertNull(vertex1.getParent());
			assertNull(m_topologyProvider.getVertex(vertex1.getParent()));
			assertNull(m_topologyProvider.getParent(vertex1));
		}

		EasyMock.verify(graphContainer);
	}

	private static void renameGroup(GraphContainer graphContainer, Vertex group, String newLabel) {
		RenameGroupOperation operation = new RenameGroupOperation();
		OperationContext context = getOperationContext(graphContainer);
		// Execute the operation on the single vertex
		operation.execute(Collections.singletonList((VertexRef)group), context);

		// Grab the window, put a value into the form field, and commit the form to complete
		// the operation.
		Window window = context.getMainWindow();
		assertEquals(1, window.getChildWindows().size());
		Window prompt = window.getChildWindows().iterator().next();

		for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
			Component component = itr.next();
			try {
				Form form = (Form)component;
				Field field = form.getField("Group Label");
				field.setValue(newLabel);
				// Make sure that the value was set, Vaadin will ignore the value
				// if, for instance, the specified value is not in the Select list
				assertEquals(newLabel, field.getValue());
				form.commit();
			} catch (ClassCastException e) {
				LoggerFactory.getLogger(GroupOperationsTest.class).info("Not a Form: " + component.getClass());
			}
		}
	}

	@Test
	public void testCreateGroupOperation() {
		m_topologyProvider.resetContainer();

		Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
		Vertex vertex2 = m_topologyProvider.addVertex(0, 0);
		Vertex group1;

		GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

		EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
		graphContainer.redoLayout();
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(graphContainer);

		assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertex1));
		assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertex2));

		{
			CreateGroupOperation groupOperation = new CreateGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			groupOperation.execute(Arrays.asList((VertexRef)vertex1, vertex2), context);

			assertNull("Value should be null: " + vertex1.getParent(), vertex1.getParent());
			assertNull("Value should be null: " + m_topologyProvider.getParent(vertex1), m_topologyProvider.getParent(vertex1));
			assertNull("Value should be null: " + vertex2.getParent(), vertex2.getParent());
			assertNull("Value should be null: " + m_topologyProvider.getParent(vertex2), m_topologyProvider.getParent(vertex2));

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field field = form.getField("Group Label");
					field.setValue("My New Awesome Group");
					// Make sure that the value was set
					assertEquals("My New Awesome Group", field.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			// Store the newly created group
			group1 = m_topologyProvider.getParent(vertex1);vertex1.getParent();

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex2));

			assertNotNull("Value should not be null: " + vertex1.getParent(), vertex1.getParent());
			assertEquals("My New Awesome Group", group1.getLabel());
			assertNotNull("Value should not be null: " + vertex2.getParent(), vertex2.getParent());
			assertEquals("My New Awesome Group", m_topologyProvider.getParent(vertex2).getLabel());

			assertEquals(2, m_topologyProvider.getChildren(group1).size());
		}

		{
			CreateGroupOperation groupOperation = new CreateGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			groupOperation.execute(Arrays.asList((VertexRef)vertex1, vertex2, group1), context);

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field field = form.getField("Group Label");
					field.setValue(m_topologyProvider.getParent(vertex1).getLabel());
					// Make sure that the value was set
					assertEquals(m_topologyProvider.getParent(vertex1).getLabel(), field.getValue());
					try {
						form.commit();
						fail("No exception thrown");
					} catch (InvalidValueException e) {
						// This is expected since the new group's label collides with the parent group
					}
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			VertexRef parent = vertex1.getParent();
			// Make sure that the parent hasn't changed after the failed create operation
			assertEquals(group1, parent);

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex2));

			assertNotNull("Value should not be null: " + parent, parent);
			assertEquals("My New Awesome Group", m_topologyProvider.getParent(vertex1).getLabel());
			assertNotNull("Value should not be null: " + vertex2.getParent(), vertex2.getParent());
			assertEquals("My New Awesome Group", m_topologyProvider.getParent(vertex2).getLabel());

			assertEquals("My New Awesome Group", m_topologyProvider.getVertex(parent).getLabel());
			assertEquals(2, m_topologyProvider.getChildren(parent).size());
		}

		{
			CreateGroupOperation groupOperation = new CreateGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			groupOperation.execute(Arrays.asList((VertexRef)vertex1, vertex2, group1), context);

			// Grab the window, put a value into the form field, and commit the form to complete
			// the operation.
			Window window = context.getMainWindow();
			assertEquals(1, window.getChildWindows().size());
			Window prompt = window.getChildWindows().iterator().next();

			for (Iterator<Component> itr = prompt.getComponentIterator(); itr.hasNext();) {
				Component component = itr.next();
				try {
					Form form = (Form)component;
					Field field = form.getField("Group Label");
					field.setValue("Oh noes a new name");
					// Make sure that the value was set
					assertEquals("Oh noes a new name", field.getValue());
					form.commit();
				} catch (ClassCastException e) {
					LoggerFactory.getLogger(this.getClass()).info("Not a Form: " + component.getClass());
				}
			}

			VertexRef parent = vertex1.getParent();
			assertEquals(parent, vertex2.getParent());
			assertEquals(parent, group1.getParent());
			assertFalse(group1.equals(parent));

			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(parent));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex2));

			assertNotNull("Value should not be null: " + parent, parent);
			assertEquals("Oh noes a new name", m_topologyProvider.getParent(vertex1).getLabel());
			assertNotNull("Value should not be null: " + vertex2.getParent(), vertex2.getParent());
			assertEquals("Oh noes a new name", m_topologyProvider.getParent(vertex2).getLabel());

			assertEquals("Oh noes a new name", m_topologyProvider.getVertex(parent).getLabel());
			assertEquals(3, m_topologyProvider.getChildren(parent).size());
		}

		{
			assertEquals(4, m_topologyProvider.getVertices().size());
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex2));

			DeleteGroupOperation groupOperation = new DeleteGroupOperation();
			OperationContext context = getOperationContext(graphContainer);
			groupOperation.execute(Collections.singletonList((VertexRef)vertex1), context);

			// Nothing happens... we tried to delete a vertex instead of a group
			assertEquals(4, m_topologyProvider.getVertices().size());
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals(1, m_topologyProvider.getSemanticZoomLevel(vertex2));

			groupOperation = new DeleteGroupOperation();
			context = getOperationContext(graphContainer);
			groupOperation.execute(Arrays.asList(vertex1.getParent()), context);

			assertEquals(3, m_topologyProvider.getVertices().size());
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(group1));
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertex1));
			assertEquals(0, m_topologyProvider.getSemanticZoomLevel(vertex2));
		}

		EasyMock.verify(graphContainer);
	}
}
