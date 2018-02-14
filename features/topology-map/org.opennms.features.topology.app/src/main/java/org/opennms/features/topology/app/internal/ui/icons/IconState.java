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

import com.vaadin.shared.ui.JavaScriptComponentState;

public class IconState extends JavaScriptComponentState {

    private List<String> elementsToShow;
    private int spacing;
    private int columnCount;
    private int maxSize;
    private String selectedIconId;

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public void setElementsToShow(List<String> elementsToShow) {
        this.elementsToShow = elementsToShow;
    }

    public String getSelectedIconId() {
        return selectedIconId;
    }

    public List<String> getElementsToShow() {
        return elementsToShow;
    }

    public int getSpacing() {
        return spacing;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setSelectedIconId(String selectedIconId) {
        this.selectedIconId = selectedIconId;
    }
}
