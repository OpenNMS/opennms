/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
