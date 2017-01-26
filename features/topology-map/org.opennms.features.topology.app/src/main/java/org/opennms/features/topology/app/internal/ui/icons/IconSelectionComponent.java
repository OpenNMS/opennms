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

package org.opennms.features.topology.app.internal.ui.icons;

import java.util.List;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;

@JavaScript({
        "theme://js/d3.v3.4.13.js",
        "theme://js/icon-selection-component_connector.js"
})
public class IconSelectionComponent extends AbstractJavaScriptComponent {

    public IconSelectionComponent(List<String> elementsToShow, String currentIconId) {
        getState().setElementsToShow(elementsToShow);
        getState().setColumnCount(5);
        getState().setMaxSize(100);
        getState().setSpacing(25);
        getState().setSelectedIconId(currentIconId);

        addFunction("onIconSelection", (JavaScriptFunction) arguments -> {
            if (arguments.length() >= 1) {
                getState().setSelectedIconId(arguments.getString(0));
            }
        });
    }

    @Override
    public IconState getState() {
        return (IconState) super.getState();
    }
}
