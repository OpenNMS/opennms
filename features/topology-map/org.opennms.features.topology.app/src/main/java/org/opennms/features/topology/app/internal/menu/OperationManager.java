/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.Operation;

public class OperationManager {

	/**
	 * {@ink Operation} registered to the OSGI service registry (including the service properties).
	 */
	private final List<OperationServiceWrapper> m_operations = new CopyOnWriteArrayList<>();

	/**
	 * Listeners listening to menu updates.
	 */
    private final List<String> m_topLevelMenuOrder = new ArrayList<>();
    private final Map<String, List<String>> m_subMenuGroupOrder = new HashMap<>();


	public void onBind(Operation operation, Map<String, String> props) {
		OperationServiceWrapper operCommand = new OperationServiceWrapper(operation, props);
		m_operations.add(operCommand);
	}

	public void onUnbind(Operation operation, Map<String, String> props) {
		for (OperationServiceWrapper command : m_operations) {
			if (command.getOperation() == operation) {
				m_operations.remove(command);
			}
		}
	}

	public void setTopLevelMenuOrder(List<String> menuOrderList) {
		if (m_topLevelMenuOrder == menuOrderList) return;
		m_topLevelMenuOrder.clear();
		m_topLevelMenuOrder.addAll(menuOrderList);

	}

    public void updateMenuConfig(Dictionary<String,?> props) {
        List<String> topLevelOrder = Arrays.asList(props.get("toplevelMenuOrder").toString().split(","));
        setTopLevelMenuOrder(topLevelOrder);

		for (String topLevelItem : topLevelOrder) {
			if (!topLevelItem.equals("Additions")) {
				String key = "submenu." + topLevelItem + ".groups";
				Object value = props.get(key);
				if (value != null) {
					addOrUpdateGroupOrder(topLevelItem, Arrays.asList(value.toString().split(",")));
				}
			}
		}
		addOrUpdateGroupOrder(
				"Default",
				Arrays.asList(props.get("submenu.Default.groups").toString().split(",")));
	}

	void addOrUpdateGroupOrder(String key, List<String> orderSet) {
		if (!m_subMenuGroupOrder.containsKey(key)) {
			m_subMenuGroupOrder.put(key, orderSet);
		} else {
			m_subMenuGroupOrder.remove(key);
			m_subMenuGroupOrder.put(key, orderSet);
		}
	}

	Map<String, List<String>> getMenuOrderConfig() {
		return m_subMenuGroupOrder;
	}

	public List<OperationServiceWrapper> getOperationWrappers() {
		return Collections.unmodifiableList(m_operations);
	}

	public List<String> getTopLevelMenuOrder() {
		return Collections.unmodifiableList(m_topLevelMenuOrder);
	}

	public Map<String, List<String>> getSubMenuGroupOrder() {
		return new HashMap<>(m_subMenuGroupOrder);
	}

	public <T extends CheckedOperation> T findOperationByLabel(String label) {
		if (label == null) {
			return null; // nothing to do
		}
		for (OperationServiceWrapper eachCommand : m_operations) {
			try {
				String opLabel = MenuBuilder.removeLabelProperties(eachCommand.getCaption());
				if (label.equals(opLabel)) {
					T operation = (T) eachCommand.getOperation();
					return operation;
				}
			} catch (ClassCastException e) {}
		}
		return null;
	}
}
