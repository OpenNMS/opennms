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
@JavaScript({
    "theme://../opennms/assets/inline-graphcontainer_connector.vaadin.js"
})
public class InlineGraphContainer extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 2L;

    public InlineGraphContainer() {
        // make sure state gets initialized
        getState();
    }

    public void setBaseHref(String baseHref) {
        getState().baseHref = baseHref;
    }

    @Override
    protected GraphContainerState getState() {
        return (GraphContainerState) super.getState();
    }
}
