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
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.UserError;
import com.vaadin.server.AbstractErrorMessage.ContentMode;
import com.vaadin.server.ErrorMessage.ErrorLevel;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class CreateGroupOperation implements Constants, Operation {

	@Override
	public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
		if (targets == null || targets.isEmpty()) return null;

		final GraphContainer graphContainer = operationContext.getGraphContainer();

		final UI window = operationContext.getMainWindow();

		final Window groupNamePrompt = new GroupWindow("Create Group", "300px", "200px");

		// Define the fields for the form
		final PropertysetItem item = new PropertysetItem();
		item.addItemProperty("Group Label", new ObjectProperty<String>("", String.class) {
			private static final long serialVersionUID = -7904501088179818863L;

			@Override
			public void setValue(String newValue) throws ReadOnlyException, ConversionException {
				if (newValue == null) super.setValue(newValue);
				if (newValue instanceof String) super.setValue(((String)newValue).trim());
			}

			@Override
			public String getValue() {
				String value = super.getValue();
				if (value != null) return value.trim();
				return value;
			}
		});		    

		final Form promptForm = new Form() {

			private static final long serialVersionUID = 8938663493202118574L;

			@Override
			public void commit() {
				// Trim the form value
				Field<String> field = getField("Group Label");
				String groupLabel = field.getValue();
				if (groupLabel == null) {
					throw new InvalidValueException("Group label cannot be null.");
				}
				getField("Group Label").setValue(groupLabel.trim());
				super.commit();
				createGroup(graphContainer, (String)getField("Group Label").getValue(), targets);
			}

			private void createGroup(final GraphContainer graphContainer, final String groupLabel, final List<VertexRef> targets) {

				// Add the new group
				VertexRef groupId = graphContainer.getBaseTopology().addGroup(groupLabel, GROUP_ICON_KEY);

				// Find a common parent group. If none can be found, then link the group to the
				// top of the topology
				Vertex parentGroup = null;
				for(VertexRef vertexRef : targets) {
					Vertex parent = graphContainer.getBaseTopology().getParent(vertexRef);
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
					graphContainer.getBaseTopology().setParent(vertexRef, groupId);
				}

				// Set the parent of the new group to the selected top-level parent
				graphContainer.getBaseTopology().setParent(groupId, parentGroup);

				// Save the topology
				operationContext.getGraphContainer().getBaseTopology().save();

				graphContainer.redoLayout();
			}
		};

		// Buffer changes to the datasource
		promptForm.setBuffered(true);
		// Bind the item to create all of the fields
		promptForm.setItemDataSource(item);
		promptForm.setDescription("Please Enter the Name of the Group");
		// Add validators to the fields
		addValidators(promptForm, graphContainer);

		// Footer
		Button ok = new Button("OK");
		ok.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 7388841001913090428L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					promptForm.validate();
					promptForm.commit();
					window.removeWindow(groupNamePrompt); // Close the prompt window
				} catch (InvalidValueException exception) {
					promptForm.setComponentError(new UserError(exception.getMessage(), ContentMode.TEXT, ErrorLevel.WARNING));
				}
			}
		});

		Button cancel = new Button("Cancel");
		cancel.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 8780989646038333243L;

			@Override
			public void buttonClick(ClickEvent event) {
				window.removeWindow(groupNamePrompt); // Close the prompt window
			}
		});

		promptForm.setFooter(new HorizontalLayout());
		promptForm.getFooter().addComponent(ok);
		promptForm.getFooter().addComponent(cancel);

		groupNamePrompt.setContent(promptForm);

		window.addWindow(groupNamePrompt);
		return null;
	}

	private void addValidators(final Form promptForm, final GraphContainer graphContainer) {
		// Add validators to the fields
		((TextField)promptForm.getField("Group Label")).setNullRepresentation("");
		((TextField)promptForm.getField("Group Label")).setValidationVisible(false);
		promptForm.getField("Group Label").setRequired(true);
		promptForm.getField("Group Label").setRequiredError("You must specify a group label.");
		promptForm.getField("Group Label").addValidator(new StringLengthValidator("The group label must be at least one character long.", 1, -1, false));

		// null validator
		promptForm.getField("Group Label").addValidator(new AbstractValidator<String>("Group label cannot be blank.") {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isValidValue(String value) {
				if (value == null) return false;
				if ( !(value instanceof String)) return false;
				return !((String)value).trim().isEmpty();
			}

			@Override
			public Class<String> getType() {
				return String.class;
			}
		});

		// unique validator
		promptForm.getField("Group Label").addValidator(new AbstractValidator<String>("A group with label \"{0}\" already exists.") {
			private static final long serialVersionUID = -2351672151921474546L;

			@Override
			protected boolean isValidValue(String value) {
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

			@Override
			public Class<String> getType() {
				return String.class;
			}
		});

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
