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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Select;
import com.vaadin.ui.Window;

public class AddVertexToGroupOperation implements Constants, Operation {
	
	private static Collection<Vertex> findGroups(GraphProvider provider, Collection<Vertex> vertices) {
		final Collection<Vertex> groups = new ArrayList<Vertex>();
		for (Vertex vertex : vertices) {
			if (vertex.isGroup()) {
				groups.add(vertex);
				LoggerFactory.getLogger(AddVertexToGroupOperation.class).debug("Found group: {}", vertex.getId());
				groups.addAll(findGroups(provider, provider.getChildren(vertex)));
			}
		}
		return groups;
	}
	
	/**
	 * This method returns all vertices which should be considered as a target. 
	 * 
	 * The returned List is created as follows:
	 * <ul>
	 *     <li>If the target is selected, than all selected vertices including the target are ŕeturned.</li>
	 *     <li>If the target is not selected, only the target is returned.</li>
	 * </ul>
	 * @param target The target.
	 * @param selectoinManager The SelectionManager.
	 * @return All vertices which should be considered as a target.
	 */
	private static Collection<VertexRef> determineTargets(final VertexRef target, final SelectionManager selectionManager) {
	    if (!selectionManager.isVertexRefSelected(target)) return Arrays.asList(target);
	    return new ArrayList<VertexRef>(selectionManager.getSelectedVertexRefs());
	}

	@Override
	public Undoer execute(List<VertexRef> targets, final OperationContext operationContext) {
	    if (targets == null || targets.isEmpty()) return null;
	    
		final Logger log = LoggerFactory.getLogger(this.getClass());
		final GraphContainer graphContainer = operationContext.getGraphContainer();

		final Collection<VertexRef> vertices = determineTargets(targets.get(0), operationContext.getGraphContainer().getSelectionManager());
		final Collection<Vertex> vertexIds = graphContainer.getBaseTopology().getRootGroup();
		final Collection<Vertex> groupIds = findGroups(graphContainer.getBaseTopology(), vertexIds);

		final Window window = operationContext.getMainWindow();
		final Window groupNamePrompt = new GroupWindow("Add This Item To a Group", "300px", "210px");

		// Define the fields for the form
		final PropertysetItem item = new PropertysetItem();
		item.addItemProperty("Group", new ObjectProperty<String>(null, String.class));

		// field factory for the form
		FormFieldFactory fieldFactory = new FormFieldFactory() {
			private static final long serialVersionUID = 2963683658636386720L;

			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				// Identify the fields by their Property ID.
				String pid = (String) propertyId;
				if ("Group".equals(pid)) {
				    final Select select = new Select("Group");
					for (Vertex childId : groupIds) {
						log.debug("Adding child: {}, {}", childId.getId(), childId.getLabel());
						select.addItem(childId.getId());
						select.setItemCaption(childId.getId(), childId.getLabel());
					}
					select.setNewItemsAllowed(false);
					select.setNullSelectionAllowed(false);
					select.setRequired(true);
					select.setRequiredError("You must select a group");
					select.addValidator(new Validator() {
                        private static final long serialVersionUID = -2466240291882827117L;

                        @Override
					    public void validate(Object value) throws InvalidValueException {
					        if (isValid(value)) return;
					        throw new InvalidValueException(String.format("You cannot add group '%s' to itself.", select.getItemCaption(value)));
					    };
					    
			            /**
			             * Ensures that if only one element is selected that this element cannot be added to itself.
			             * If there are more than one elements selected, we assume as valid. 
			             */
					    @Override
			            public boolean isValid(Object value) {
			                if (vertices.size() > 1) return true; // more than 1 -> assume valid
			                final String groupId = (String)select.getValue();
			                // only one, check if we want to assign to ourself
			                for (VertexRef eachVertex : vertices) {
			                    if (groupId.equals(eachVertex.getId())) {
			                        return false;
			                    }
			                }
			                return true;
			            }
			        });
					return select;
				}
				return null; // Invalid field (property) name.
			}
		};

		// create the form
		final Form promptForm = new Form() {
            private static final long serialVersionUID = 8310646938173207767L;

            @Override
            public void commit() throws SourceException, InvalidValueException {
                super.commit();
                String groupId = (String)getField("Group").getValue();
                Vertex group = graphContainer.getBaseTopology().getVertex(graphContainer.getBaseTopology().getVertexNamespace(), groupId);
                log.debug("Field value: {}", group.getId());
                for (VertexRef eachChild : vertices) {
                    if (eachChild == group) {
                        log.warn("Ignoring group:(id={},label={}), because otherwise we should add it to itself.", eachChild.getId(), eachChild.getLabel());
                        continue;
                    }
                    log.debug("Adding item:(id={},label={}) to group:(id={},label={})", eachChild.getId(), eachChild.getLabel(), group.getId(), group.getLabel());
                    graphContainer.getBaseTopology().setParent(eachChild, group);
                }
                graphContainer.getBaseTopology().save();
                graphContainer.redoLayout();
            }
		};
		promptForm.setWriteThrough(false);
		promptForm.setFormFieldFactory(fieldFactory);
		promptForm.setItemDataSource(item);
		promptForm.setDescription("Please select a group.");

		// Footer
		Button ok = new Button("OK");
		ok.addListener(new ClickListener() {
			private static final long serialVersionUID = 7388841001913090428L;

			@Override
			public void buttonClick(ClickEvent event) {
			    try {
			        promptForm.validate();
			        promptForm.commit();
			        window.removeWindow(groupNamePrompt);   // Close the prompt window
			    } catch (InvalidValueException exception) {
			        promptForm.setComponentError(new UserError(exception.getMessage(), UserError.CONTENT_TEXT, exception.getErrorLevel()));
			    }
			}
		});

		Button cancel = new Button("Cancel");
		cancel.addListener(new ClickListener() {
			private static final long serialVersionUID = 8780989646038333243L;

			@Override
			public void buttonClick(ClickEvent event) {
				window.removeWindow(groupNamePrompt); // Close the prompt window
			}
		});
		
		promptForm.setFooter(new HorizontalLayout());
		promptForm.getFooter().addComponent(ok);
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
		return targets.size() == 1;
	}

	@Override
	public String getId() {
		return null;
	}

}
