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

import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.Operation;

/**
 * Helper object to encapsulate the registered {@link Operation} with its service properties, as this is
 * needed to build the MenuBar and ContextMenu for the Topology Map.
 *
 * @author mvrueden
 */
public class OperationServiceWrapper {

    private Operation m_operation;
    private Map<String, String> m_props;
    
    public OperationServiceWrapper(Operation operation, Map<String, String> props) {
        m_operation = Objects.requireNonNull(operation);
        m_props = Objects.requireNonNull(props);
    }

    public String getMenuPosition() {
        String menuLocation = m_props.get(Operation.OPERATION_MENU_LOCATION);
        return menuLocation == null || menuLocation.isEmpty() ? null : menuLocation;
    }

    public Operation getOperation() {
        return m_operation;
    }

	public String getContextMenuPosition() {
		String contextLocation = m_props.get(Operation.OPERATION_CONTEXT_LOCATION);
		return contextLocation == null ? null : contextLocation.isEmpty() ? "" : contextLocation;
	}

    @Override
    public String toString() {
        return getCaption();
    }

    public String getCaption() {
        return m_props.get(Operation.OPERATION_LABEL);
    }
}
