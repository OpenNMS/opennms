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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class NodeMapState extends AbstractComponentState {
    private static final long serialVersionUID = 7166424509065088284L;
    public String searchString;
    public List<MapNode> nodes = new LinkedList<>();
    public List<Integer> nodeIds = new ArrayList<>();
    public int minimumSeverity;
    public boolean groupByState = true;
    public int maxClusterRadius = 350;
    public List<Option> tileLayerOptions = new ArrayList<>();
    public String tileServerUrl;
}
