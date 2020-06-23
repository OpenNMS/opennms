/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client.ui;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

import java.util.List;

public class SuggestionMenu extends MenuBar {

    public SuggestionMenu() {

    }

    public int getSelectedItemIndex() {
        // The index of the currently selected item can only be
        // obtained if the menu is showing.
        MenuItem selectedItem = getSelectedItem();
        if (selectedItem != null) {
            return getItems().indexOf(selectedItem);
        }
        return -1;
    }

    public SuggestionMenuItem getSelectedItem(){
        return (SuggestionMenuItem)super.getSelectedItem();
    }

    public void selectItem(int index) {
        List<MenuItem> items = getItems();
        if (index > -1 && index < items.size()) {
            //itemOver(items.get(index), false);
        }
    }

    public int getNumItems() {
        return getItems().size();
    }

}
