/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.components.graph;

import java.util.Date;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Graph container that allows pre-fabricated graphs to be
 * rendered using different graphing engines.
 *
 * See js/graph.js for details.
 *
 * @author jwhite
 */
@JavaScript({
    "theme://../opennms/assets/graphcontainer_connector.vaadin.js"
})
public class GraphContainer extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 2L;

    public GraphContainer(final String graphName, final String resourceId) {
        final GraphContainerState state = getState();
        state.graphName = graphName;
        state.resourceId = resourceId;

        setWidth(100, Unit.PERCENTAGE);
    }

    public void setBaseHref(String baseHref) {
        getState().baseHref = baseHref;
    }

    public void setStart(Date start) {
        getState().start = start.getTime();
    }

    public void setEnd(Date end) {
        getState().end = end.getTime();
    }

    public void setWidthRatio(Double widthRatio) {
        getState().widthRatio = widthRatio;
    }

    public void setHeightRatio(Double heightRatio) {
        getState().heightRatio = heightRatio;
    }

    public void setTitle(String title) {
        getState().title = title;
    }

    @Override
    protected GraphContainerState getState() {
        return (GraphContainerState) super.getState();
    }
}
