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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphProvider;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class GroupOperationsTest {

	private static class TestOperationContext implements OperationContext {

		private final GraphContainer m_graphContainer;
		private final UI m_window;

		public TestOperationContext(GraphContainer graphContainer) {
			m_graphContainer = graphContainer;
			m_window = new UI() {
				@Override
				protected void init(VaadinRequest request) {
				}};
		}

		@Override
		public UI getMainWindow() {
			return m_window;
		}

		@Override
		public GraphContainer getGraphContainer() {
			return m_graphContainer;
		}

		@Override
		public boolean isChecked() {
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
	
	private static Form getForm(final Window prompt) {
	    for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
            Component component = itr.next();
            if (component instanceof Form) return (Form)component;
            LoggerFactory.getLogger(GroupOperationsTest.class).info("Not a Form: " + component.getClass());
        }
	    throw new IllegalArgumentException("prompt does not have a form");
	}

	private static Window getPrompt(final OperationContext context) {
	    UI window = context.getMainWindow();
        assertEquals(1, window.getWindows().size());
        Window prompt = window.getWindows().iterator().next();
        assertNotNull(prompt);
        return prompt;
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
		SelectionManager selectionManager = EasyMock.createNiceMock(SelectionManager.class);
		EasyMock.expect(selectionManager.isVertexRefSelected(EasyMock.anyObject(VertexRef.class))).andReturn(false).anyTimes();
		EasyMock.expect(graphContainer.getSelectionManager()).andReturn(selectionManager).anyTimes();
		graphContainer.redoLayout();
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(graphContainer);
		EasyMock.replay(selectionManager);

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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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

		EasyMock.verify(graphContainer, selectionManager);
	}

	private static void renameGroup(GraphContainer graphContainer, Vertex group, String newLabel) {
		RenameGroupOperation operation = new RenameGroupOperation();
		OperationContext context = getOperationContext(graphContainer);
		// Execute the operation on the single vertex
		operation.execute(Collections.singletonList((VertexRef)group), context);

		// Grab the window, put a value into the form field, and commit the form to complete
		// the operation.
		UI window = context.getMainWindow();
		assertEquals(1, window.getWindows().size());
		Window prompt = window.getWindows().iterator().next();

		for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
	// We try to add a group to its own. This should fail!
	public void testAddGroupToItself() {
	    m_topologyProvider.resetContainer();
	    Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
	    Vertex vertex2 = m_topologyProvider.addVertex(0, 0);
	    Vertex group1 = m_topologyProvider.addGroup("group1",  "group");
	    m_topologyProvider.setParent(vertex1,  group1);
        
	    // we try to add the group to itself. There is no selection
	    {
	        GraphContainer graphContainer = EasyMock.createNiceMock(GraphContainer.class);
    	    EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
    	    EasyMock.expect(graphContainer.getSelectionManager()).andReturn(EasyMock.createNiceMock(SelectionManager.class)).anyTimes();
            graphContainer.redoLayout();
            EasyMock.expectLastCall().anyTimes();
            EasyMock.replay(graphContainer);
    	    
            AddVertexToGroupOperation operation = new AddVertexToGroupOperation();
            OperationContext context = getOperationContext(graphContainer);
            operation.execute(Arrays.asList((VertexRef)group1), context);
    
            // Grab the window, put a value into the form field, and commit the form to complete
            // the operation.
            Form form = getForm(getPrompt(context));
            
            
            Field field = form.getField("Group");
            field.setValue(group1.getId());
            Assert.assertEquals(group1.getId(), field.getValue());         // Make sure that the value was set
            try {
                form.commit();
                fail("An " + InvalidValueException.class + " should have been thrown.");
            } catch (InvalidValueException ex) {
                LoggerFactory.getLogger(getClass()).info("Exception occured as expected.", ex);
            }
            EasyMock.verify(graphContainer);
	    }
        
    
        // we try to add the group to itself. There are multiple selections
	    {
            GraphContainer graphContainer = EasyMock.createNiceMock(GraphContainer.class);
            EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
            SelectionManager selectionManager = EasyMock.createNiceMock(SelectionManager.class);
            EasyMock.expect(selectionManager.isVertexRefSelected(EasyMock.anyObject(VertexRef.class))).andReturn(true).anyTimes();
            EasyMock.expect(selectionManager.getSelectedVertexRefs()).andReturn(Arrays.asList((VertexRef)vertex1, vertex2, group1)).anyTimes();
            EasyMock.expect(graphContainer.getSelectionManager()).andReturn(selectionManager).anyTimes();
            graphContainer.redoLayout();
            EasyMock.expectLastCall().anyTimes();
            EasyMock.replay(graphContainer);
            EasyMock.replay(selectionManager);
            
            AddVertexToGroupOperation operation = new AddVertexToGroupOperation();
            OperationContext context = getOperationContext(graphContainer);
            operation.execute(Arrays.asList((VertexRef)group1), context);
    
            // Grab the window, put a value into the form field, and commit the form to complete
            // the operation.
            Form form = getForm(getPrompt(context));
            
            // we try to add the group to itself. There is no selection
            Field field = form.getField("Group");
            field.setValue(group1.getId());
            Assert.assertEquals(group1.getId(), field.getValue());         // Make sure that the value was set
            form.commit();
            
            // v0 and v1 should be children of g0
            Assert.assertEquals(group1, vertex1.getParent()); //v0
            Assert.assertEquals(group1, vertex2.getParent()); //v1
            
            // g0 should not be a children of g0
            Assert.assertNull(group1.getParent());
            
            EasyMock.verify(graphContainer, selectionManager);
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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
			group1 = m_topologyProvider.getParent(vertex1);

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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
			UI window = context.getMainWindow();
			assertEquals(1, window.getWindows().size());
			Window prompt = window.getWindows().iterator().next();

			for (Iterator<Component> itr = prompt.iterator(); itr.hasNext();) {
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
	
	/**
	 * This test creates two groups:
	 * <ol>
	 * 	<li>an empty group 'group1'</li>
	 * <li>a group 'group2' with two nodes ('node1' and 'node2')
	 * </ol>
	 * We add group2 to group1 and the nodes of group2 should not be added to group1. 
	 * They should still be assigned to group2
	 * 
	 */
	@Test
	public void testAddGroupWithNodesToAnotherGroup() {
		 m_topologyProvider.resetContainer();
		 Vertex node1 = m_topologyProvider.addVertex(0, 0);
		 Vertex node2 = m_topologyProvider.addVertex(0, 0);
		 Vertex group1 = m_topologyProvider.addGroup("group1",  "group");
		 Vertex group2 = m_topologyProvider.addGroup("group1",  "group");
		 m_topologyProvider.setParent(node1, group2);
		 m_topologyProvider.setParent(node2, group2);
	        
		 // we try to add group2 to group1
		 {
			 GraphContainer graphContainer = EasyMock.createNiceMock(GraphContainer.class);
	         EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
	         SelectionManager selectionManager = EasyMock.createNiceMock(SelectionManager.class);
	         EasyMock.expect(selectionManager.isVertexRefSelected(EasyMock.anyObject(VertexRef.class))).andReturn(true).anyTimes();
	         EasyMock.expect(selectionManager.getSelectedVertexRefs()).andReturn(Arrays.asList((VertexRef)node1, node2, group2)).anyTimes();
	         EasyMock.expect(graphContainer.getSelectionManager()).andReturn(selectionManager).anyTimes();
	         graphContainer.redoLayout();
	         EasyMock.expectLastCall().anyTimes();
	         EasyMock.replay(graphContainer);
	         EasyMock.replay(selectionManager);
	            
	         AddVertexToGroupOperation operation = new AddVertexToGroupOperation();
	         OperationContext context = getOperationContext(graphContainer);
	         operation.execute(Arrays.asList((VertexRef)group2), context);
	    
	         // Grab the window, put a value into the form field, and commit the form to complete
	         // the operation.
	         Form form = getForm(getPrompt(context));
	            
	         // we try to add the group to itself. There is no selection
	         Field field = form.getField("Group");
	         field.setValue(group1.getId());
	         Assert.assertEquals(group1.getId(), field.getValue());         // Make sure that the value was set
	         form.commit();
	            
	         // verify
	         Assert.assertEquals(group1, group2.getParent()); // group2 should be child of group1
	         Assert.assertEquals(group2, node1.getParent()); // node1 is still child of group2
	         Assert.assertEquals(group2, node2.getParent()); // node 2 is still child of group2
	         Assert.assertNull(group1.getParent()); 	         // group 1 has no parent
	            
	         EasyMock.verify(graphContainer, selectionManager);
		 }
	}
}
