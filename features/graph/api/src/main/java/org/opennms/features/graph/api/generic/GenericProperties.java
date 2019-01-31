/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.graph.api.generic;

// Very similar to the ones from OpenNMS, but only generic ones, no special meaning can be found here
public interface GenericProperties {
    String ID = "id";
    String DESCRIPTION = "description";
    String NAMESPACE = "namespace";
    @Deprecated
    String ICON_KEY = "iconKey";
    String IP_ADDRESS = "ipAddr";
    String LABEL = "label";
    String LOCKED = "locked";
    String NODE_ID = "nodeID";
    String FOREIGN_SOURCE = "foreignSource";
    String FOREIGN_ID = "foreignID";
    @Deprecated
    String SELECTED = "selected";
    @Deprecated
    String STYLE_NAME = "styleName";
    @Deprecated
    String TOOLTIP = "tooltip";
    String X = "x";
    String Y = "y";
    String PREFERRED_LAYOUT = "preferred-layout";
    String FOCUS_STRATEGY = "focus-strategy";
    String FOCUS_IDS = "focus-ids";
    String SEMANTIC_ZOOM_LEVEL = "semantic-zoom-level";
    String VERTEX_STATUS_PROVIDER = "vertex-status-provider";
    String LEVEL = "level";
    String EDGE_PATH_OFFSET = "edge-path-offset";
    String BREADCRUMB_STRATEGY = "breadcrumb-strategy";
    String LOCATION = "location";

    // Reference to a node, either the id, or a <foreignSource>:<foreignId> statement
    String NODE_REF = "nodeRef";
}
