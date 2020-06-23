/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client;

import java.util.List;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;

public interface TopologyComponentServerRpc extends ServerRpc {
    
    void doubleClicked(MouseEventDetails eventDetails);
    void deselectAllItems();
    void edgeClicked(String edgeId);
    void backgroundClicked();
    void scrollWheel(double scrollVal, int x, int y);
    void mapPhysicalBounds(int width, int height);
    void marqueeSelection(String[] vertexIds, MouseEventDetails eventDetails);
    void contextMenu(String target, String type, int x, int y);
    void clientCenterPoint(int x, int y);
    void vertexClicked(String vertexId, MouseEventDetails eventDetails, String platform);
    void updateVertices(List<String> vertices);
    void backgroundDoubleClick(double x, double y);
}
