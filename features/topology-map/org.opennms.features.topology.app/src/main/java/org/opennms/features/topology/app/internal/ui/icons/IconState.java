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
