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
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator.InvalidValueException;
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
				String groupLabel = ((String)getField("Group Label").getValue());
				if (groupLabel == null) {
					throw new InvalidValueException("Group label cannot be null.");
				}
				getField("Group Label").setValue(groupLabel.trim());
				super.commit();
				groupLabel = (String)getField("Group Label").getValue();

				// Add the new group
				VertexRef groupId = operationContext.getGraphContainer().getBaseTopology().addGroup(groupLabel, GROUP_ICON_KEY);

				// Find a common parent group. If none can be found, then link the group to the
				// top of the topology
				Vertex parentGroup = null;
				for(VertexRef vertexRef : targets) {
					Vertex parent = operationContext.getGraphContainer().getBaseTopology().getParent(vertexRef);
					if (parentGroup == null) {
						parentGroup = parent;
					} else if (!parentGroup.equals(parent)) {
						// If there are multiple parents present then attach the new group 
						// to the top level of the hierarchy
						parentGroup = null;
						break;
					}
				}

				// Link all targets to the newly-created group
				for(VertexRef vertexRef : targets) {
					operationContext.getGraphContainer().getBaseTopology().setParent(vertexRef, groupId);
				}

				// Set the parent of the new group to the selected top-level parent
				operationContext.getGraphContainer().getBaseTopology().setParent(groupId, parentGroup);

				// Save the topology
				operationContext.getGraphContainer().getBaseTopology().save();

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
					final Collection<? extends Vertex> vertexIds = graphContainer.getBaseTopology().getVertices();
					final Collection<String> groupLabels = new ArrayList<String>();
					for (Vertex vertexId : vertexIds) {
						if (vertexId.isGroup()) {
							groupLabels.add(vertexId.getLabel());
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
}
