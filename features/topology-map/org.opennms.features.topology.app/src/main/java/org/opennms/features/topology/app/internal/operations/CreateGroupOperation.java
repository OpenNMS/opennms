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

import java.util.List;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


public class CreateGroupOperation implements Constants, Operation {

	public TopologyProvider m_topologyProvider;

	public CreateGroupOperation(TopologyProvider topologyProvider) {
		m_topologyProvider = topologyProvider;
	}

	@Override
	public Undoer execute(final List<Object> targets, final OperationContext operationContext) {
		if (targets == null || targets.isEmpty()) {
			return null;
		}

		final GraphContainer graphContainer = operationContext.getGraphContainer();

		final Window window = operationContext.getMainWindow();

		final Window groupNamePrompt = new Window();
		groupNamePrompt.setModal(false);

		// Define the fields for the form
		final PropertysetItem item = new PropertysetItem();
		item.addItemProperty("Group Label", new ObjectProperty<String>("", String.class));

		// TODO Add validator for groupname value

		final Form promptForm = new Form() {

			private static final long serialVersionUID = 2067414790743946906L;

			@Override
			public void commit() {
				super.commit();
				String groupLabel = (String)getField("Group Label").getValue();

				Object groupId = m_topologyProvider.addGroup(groupLabel, GROUP_ICON_KEY);

				/*
				for(Object itemId : targets) {
					m_topologyProvider.setParent(itemId, groupId);
				}
				 */

				Object parentGroup = null;
				for(Object key : targets) {
					Object vertexId = graphContainer.getVertexItemIdForVertexKey(key);
					Object parent = m_topologyProvider.getVertexContainer().getParent(vertexId);
					if (parentGroup == null) {
						parentGroup = parent;
					} else if (parentGroup != parent) {
						parentGroup = ROOT_GROUP_ID;
					}
					m_topologyProvider.setParent(vertexId, groupId);
				}

				m_topologyProvider.setParent(groupId, parentGroup == null ? ROOT_GROUP_ID : parentGroup);

				// Save the topology
				m_topologyProvider.save(null);
			}
		};
		// Buffer changes to the datasource
		promptForm.setWriteThrough(false);
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
	public boolean display(List<Object> targets, OperationContext operationContext) {
		return true;
	}

	@Override
	public boolean enabled(List<Object> targets, OperationContext operationContext) {
		return targets.size() > 0;
	}

	@Override
	public String getId() {
		return null;
	}
}
