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

package org.opennms.features.topology.app.internal.gwt.client.view;

import org.opennms.features.topology.app.internal.gwt.client.GWTBoundingBox;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent;
import org.opennms.features.topology.app.internal.gwt.client.VTopologyComponent.GraphUpdateListener;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGGElement;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGMatrix;
import org.opennms.features.topology.app.internal.gwt.client.svg.SVGPoint;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public interface TopologyView<T extends VTopologyComponent.TopologyViewRenderer> {
    
    public static final int LEFT_MARGIN = 60;
    
    public interface Presenter<T>{
        void addGraphUpdateListener(GraphUpdateListener listener);
        T getViewRenderer();
        void onContextMenu(Object element, int x, int y, String type);
        void onMouseWheel(double newScale, int x, int y);
        void onBackgroundClick();
        void onBackgroundDoubleClick(SVGPoint center);
    }
    
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
    SVGElement getSVGElement();
    SVGGElement getSVGViewPort();
    Element getEdgeGroup();
    Element getVertexGroup();
    Element getMarqueeElement();
    SVGMatrix calculateNewTransform(GWTBoundingBox bound);
    SVGPoint getCenterPos(GWTBoundingBox gwtBoundingBox);
    int getPhysicalWidth();
    int getPhysicalHeight();
    SVGPoint getPoint(int clientX, int clientY);
}
