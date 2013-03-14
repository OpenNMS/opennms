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

import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Select;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


public class RemoveVertexFromGroupOperation implements Constants, Operation {
	
	@Override
	public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
		if (targets == null || targets.isEmpty() || targets.size() != 1) {
			return null;
		}

		final Logger log = LoggerFactory.getLogger(this.getClass());

		final GraphContainer graphContainer = operationContext.getGraphContainer();

		final VertexRef currentGroup = targets.get(0);
		final Collection<? extends Vertex> children = graphContainer.getBaseTopology().getChildren(currentGroup);
		for (Vertex child : children) {
			log.debug("Child ID: {}", child.getId());
		}

		final Window window = operationContext.getMainWindow();

		final Window groupNamePrompt = new Window("Remove Item From Group");
		groupNamePrompt.setModal(true);
		groupNamePrompt.setResizable(false);
		groupNamePrompt.setHeight("180px");
		groupNamePrompt.setWidth("300px");

		// Define the fields for the form
		final PropertysetItem item = new PropertysetItem();
		item.addItemProperty("Item", new ObjectProperty<String>(null, String.class));

		FormFieldFactory fieldFactory = new FormFieldFactory() {
			private static final long serialVersionUID = 243277720538924081L;

			public Field createField(Item item, Object propertyId, Component uiContext) {
				// Identify the fields by their Property ID.
				String pid = (String) propertyId;
				if ("Item".equals(pid)) {
					Select select = new Select("Item");
					for (Vertex child : children) {
						log.debug("Adding child: {}, {}", child.getId(), child.getLabel());
						select.addItem(child.getId());
						select.setItemCaption(child.getId(), child.getLabel());
					}
					select.setNewItemsAllowed(false);
					select.setNullSelectionAllowed(false);
					return select;
				}

				return null; // Invalid field (property) name.
			}
		};

		// TODO Add validator for name value

		final Form promptForm = new Form() {

			private static final long serialVersionUID = 2067414790743946906L;

			@Override
			public void commit() {
				super.commit();

				String childId = (String)getField("Item").getValue();
				log.debug("Field value: {}", childId);

				LoggerFactory.getLogger(this.getClass()).debug("Removing item from group: {}", childId);

				Vertex grandParent = graphContainer.getBaseTopology().getParent(currentGroup);

				GraphProvider topologyProvider = graphContainer.getBaseTopology();

				// Relink the child to the grandparent group (or null if it is null)
				topologyProvider.setParent(graphContainer.getBaseTopology().getVertex(graphContainer.getBaseTopology().getVertexNamespace(), childId), grandParent);

				// Save the topology
				topologyProvider.save();

				graphContainer.redoLayout();
			}
		};
		// Buffer changes to the datasource
		promptForm.setWriteThrough(false);
		// You must set the FormFieldFactory before you set the data source
		promptForm.setFormFieldFactory(fieldFactory);
		promptForm.setItemDataSource(item);

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
		return (targets.size() == 1) && (operationContext.getGraphContainer().getBaseTopology().hasChildren(targets.get(0)));
	}

	@Override
	public String getId() {
		return "RemoveVertexFromGroup";
	}
}
