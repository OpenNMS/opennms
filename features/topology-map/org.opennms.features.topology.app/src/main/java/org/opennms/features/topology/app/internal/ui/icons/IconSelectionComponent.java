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

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;

@JavaScript({
    "theme://../opennms/assets/icon-selection-component_connector.vaadin.js"
})
public class IconSelectionComponent extends AbstractJavaScriptComponent {
    private static final long serialVersionUID = 1L;

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
