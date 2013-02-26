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

package org.opennms.features.topology.app.internal.gwt.client.handler;

import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.TopologyViewRenderer;
import org.opennms.features.topology.app.internal.gwt.client.service.ServiceRegistry;
import org.opennms.features.topology.app.internal.gwt.client.view.TopologyView;

import com.google.gwt.dom.client.Element;

public class PanHandler implements DragBehaviorHandler{
    public static String DRAG_BEHAVIOR_KEY = "panHandler";
    protected PanObject m_panObject;
    TopologyView<TopologyViewRenderer> m_topologyView;
    ServiceRegistry m_serviceRegistry;
    VTopologyComponent m_topologyComponent;
    
    public PanHandler(VTopologyComponent vtopologyComp, ServiceRegistry serviceRegistry) {
        m_topologyComponent = vtopologyComp;
        m_topologyView = vtopologyComp.getTopologyView();
        m_serviceRegistry = serviceRegistry;
    }
    
    @Override
    public void onDragStart(Element elem) {
        m_panObject = new PanObject(m_topologyView);
    }

    @Override
    public void onDrag(Element elem) {
        m_panObject.move();
    }

    @Override
    public void onDragEnd(Element elem) {
        m_panObject = null;
        m_topologyComponent.updateMapPosition();
    }

    
}