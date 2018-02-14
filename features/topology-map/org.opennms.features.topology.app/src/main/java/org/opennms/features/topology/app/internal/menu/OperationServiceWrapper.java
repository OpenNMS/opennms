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
