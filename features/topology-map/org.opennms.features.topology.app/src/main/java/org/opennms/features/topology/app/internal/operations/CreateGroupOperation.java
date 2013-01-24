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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


public class CreateGroupOperation implements Constants, Operation {

	@Override
	public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
		if (targets == null || targets.isEmpty()) {
			return null;
		}

		final GraphContainer graphContainer = operationContext.getGraphContainer();

		final Window window = operationContext.getMainWindow();

		final Window groupNamePrompt = new Window("Create Group");
		groupNamePrompt.setModal(true);
		groupNamePrompt.setResizable(false);
		groupNamePrompt.setHeight("220px");
		groupNamePrompt.setWidth("300px");

		// Define the fields for the form
		final PropertysetItem item = new PropertysetItem();
		item.addItemProperty("Group Label", new ObjectProperty<String>("", String.class));

		final Form promptForm = new Form() {

			private static final long serialVersionUID = 2067414790743946906L;

			@Override
			public void commit() {
				// Trim the form value
				getField("Group Label").setValue(((String)getField("Group Label").getValue()).trim());
				super.commit();
				String groupLabel = (String)getField("Group Label").getValue();

				TopologyProvider topologyProvider = graphContainer.getDataSource();
				// Add the new group
				Object groupId = topologyProvider.addGroup(groupLabel, GROUP_ICON_KEY);

				Object parentGroup = null;
				for(VertexRef vertexRef : targets) {
					Object vertexId = getTopoItemId(graphContainer, vertexRef);
					Object parent = topologyProvider.getVertexContainer().getParent(vertexId);
					if (parentGroup == null) {
						parentGroup = parent;
					} else if (!parentGroup.equals(parent)) {
						parentGroup = ROOT_GROUP_ID;
					}
					topologyProvider.setParent(vertexId, groupId);
				}

				// Set the parent of the new group to the selected top-level parent
				topologyProvider.setParent(groupId, parentGroup == null ? ROOT_GROUP_ID : parentGroup);

				// Save the topology
				topologyProvider.save(null);

				graphContainer.redoLayout();
			}
		};
		// Buffer changes to the datasource
		promptForm.setWriteThrough(false);
		// Bind the item to create all of the fields
		promptForm.setItemDataSource(item);
		// Add validators to the fields
		promptForm.getField("Group Label").setRequired(true);
		promptForm.getField("Group Label").setRequiredError("Group label cannot be blank.");
		promptForm.getField("Group Label").addValidator(new StringLengthValidator("Label must be at least one character long.", 1, -1, false));
		promptForm.getField("Group Label").addValidator(new AbstractValidator("A group with label \"{0}\" already exists.") {

			private static final long serialVersionUID = -6602249815731561328L;

			@Override
			public boolean isValid(Object value) {
				try {
					final Collection<String> vertexIds = (Collection<String>)graphContainer.getDataSource().getVertexContainer().getItemIds();
					final Collection<String> groupLabels = new ArrayList<String>();
					for (String vertexId : vertexIds) {
						BeanItem<?> vertex = graphContainer.getDataSource().getVertexContainer().getItem(vertexId);
						if (!(Boolean)vertex.getItemProperty("leaf").getValue()) {
							groupLabels.add((String)vertex.getItemProperty("label").getValue());
						}
					}

					for (String label : groupLabels) {
						LoggerFactory.getLogger(this.getClass()).debug("Comparing {} to {}", value, label);
						if (label.equals(value)) {
							return false;
						}
					}
					return true;
				} catch (Throwable e) {
					LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
					return false;
				}
			}
		});

		Button ok = new Button("OK");
		ok.addListener(new ClickListener() {

			private static final long serialVersionUID = 7388841001913090428L;

			@Override
			public void buttonClick(ClickEvent event) {
				promptForm.commit();
				// Close the prompt window
				window.removeWindow(groupNamePrompt);
			}
		});
		promptForm.getFooter().addComponent(ok);

		Button cancel = new Button("Cancel");
		cancel.addListener(new ClickListener() {

			private static final long serialVersionUID = 8780989646038333243L;

			@Override
			public void buttonClick(ClickEvent event) {
				// Close the prompt window
				window.removeWindow(groupNamePrompt);
			}
		});
		promptForm.getFooter().addComponent(cancel);

		groupNamePrompt.addComponent(promptForm);

		window.addWindow(groupNamePrompt);

		return null;
	}

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
		return true;
	}

	@Override
	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
		return targets.size() > 0;
	}

	@Override
	public String getId() {
		return null;
	}

	private Object getTopoItemId(GraphContainer graphContainer, VertexRef vertexRef) {
		if (vertexRef == null)  return null;
		Vertex v = graphContainer.getVertex(vertexRef);
		if (v == null) return null;
		Item item = v.getItem();
		if (item == null) return null;
		Property property = item.getItemProperty("itemId");
		return property == null ? null : property.getValue();
	}
}
