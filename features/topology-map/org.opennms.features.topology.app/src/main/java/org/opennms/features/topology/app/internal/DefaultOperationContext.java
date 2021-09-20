/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.ui.UI;

/**
 * Default implementation.
 */
public class DefaultOperationContext implements OperationContext {

    private final UI m_mainWindow;
    private final GraphContainer m_graphContainer;
    private final DisplayLocation m_displayLocation;

    public DefaultOperationContext(UI mainWindow, GraphContainer graphContainer, DisplayLocation displayLocation) {
        m_mainWindow = mainWindow;
        m_graphContainer = graphContainer;
        m_displayLocation = displayLocation;
    }

    @Override
    public UI getMainWindow() {
        return m_mainWindow;
    }

    @Override
    public GraphContainer getGraphContainer() {
        return m_graphContainer;
    }

    @Override
    public DisplayLocation getDisplayLocation() {
        return m_displayLocation;
    }
}
