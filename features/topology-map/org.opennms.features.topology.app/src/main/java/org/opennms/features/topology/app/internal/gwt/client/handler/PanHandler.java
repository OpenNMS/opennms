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

package org.opennms.features.topology.app.internal.gwt.client.handler;

import java.util.Objects;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.gwt.client.d3.D3;

import com.google.gwt.dom.client.Element;

public class PanHandler implements DragBehaviorHandler{
    public static String DRAG_BEHAVIOR_KEY = "panHandler";
    protected PanObject m_panObject;
    VTopologyComponent m_topologyComponent;
    
    public PanHandler(VTopologyComponent vTopologyComponent) {
        m_topologyComponent = Objects.requireNonNull(vTopologyComponent);
    }

    @Override
    public void onDragStart(Element elem) {
        m_panObject = new PanObject(m_topologyComponent.getTopologyView());
        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }

    @Override
    public void onDrag(Element elem) {
        m_panObject.move();
        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }

    @Override
    public void onDragEnd(Element elem) {
        m_panObject = null;
        m_topologyComponent.updateMapPosition();
        D3.getEvent().stopPropagation();
        D3.getEvent().preventDefault();
    }
}