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

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Graph container that allows graphs to be rendered using different graphing
 * engines.
 *
 * See js/graph.js for details.
 *
 * @author jwhite
 * @author fooker
 */
// Vaadin doensn't allow us to reference .js files outside of the application using relative paths
// so we resort to copying the minimal dependencies into the target .jar and importing them here.
// The resources are copied using the maven-resources-plugin definition in this module's pom.xml.
// Only resources required to bootstrap graph.js should be included here - others should
// be loaded dynamically.
@JavaScript({
    "require.js",
    "global.js",
    "jquery.js",
    "graph.js",
    "graphcontainer-connector.js"
})
public class InlineGraphContainer extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 4363043899957566308L;

    public InlineGraphContainer() {
        final GraphContainerState state = getState();
    }

    public void setBaseHref(String baseHref) {
        getState().baseHref = baseHref;
    }

    @Override
    protected GraphContainerState getState() {
        return (GraphContainerState) super.getState();
    }
}
