/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
