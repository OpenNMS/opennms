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
package org.opennms.features.topology.app.internal.menu;

import java.util.Comparator;
import java.util.List;

import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.MenuBar;

/**
 * Topology UI MenuItem API.
 * This allows a fine grade control over each Menu Item, e.g. the order, label, etc.
 *
 * @author mvrueden
 */
public interface MenuItem extends Comparable<MenuItem> {
    /**
     * The Label of the menu item.
     *
     * @return The label of the menu item.
     */
    String getLabel();

    /**
     * Defines if the current {@link MenuItem} is visible.
     *
     * @param targets The current targets (e.g. the selection)
     * @param operationContext The current {@link OperationContext}.
     * @return True if visible, false otherwise.
     */
    boolean isVisible(List<VertexRef> targets, OperationContext operationContext);

    /**
     * Defines if the current {@link MenuItem} is enabled.
     * If {@link #isVisible(List, OperationContext)} returns false, this is never called.
     *
     * @param targets The current targets (e.g. the selection)
     * @param operationContext The current {@link OperationContext}.
     * @return True if enabled, false otherwise.
     */
    boolean isEnabled(List<VertexRef> targets, OperationContext operationContext);

    /**
     * Defines if the current {@link MenuItem} is checked.
     * If {@link #isCheckable()} returns false, this is never called.
     *
     * @param targets The current targets (e.g. the selection)
     * @param operationContext The current {@link OperationContext}.
     * @return True if it is checked, false otherwise.
     */
    boolean isChecked(List<VertexRef> targets, OperationContext operationContext);

    void setOrder(int order);

    int getOrder();

    /**
     * Defines if the current {@link MenuItem} is a checkable item.
     *
     * @return true if checkable, false otherwise
     * @see MenuBar.MenuItem#isCheckable()
     */
    boolean isCheckable();


    /**
     * The {@link MenuItem}s can be cascaded (allowing a tree).
     *
     * @return the children, or an empty list if no children exits. Must not be null.
     */
    List<MenuItem> getChildren();

    /**
     * Adds a children to the current {@link MenuItem}
     *
     * @param menuItem the child to add.
     */
    void addChildren(MenuItem menuItem);

    /**
     * Returns the command to execute if the {@link MenuItem} is selected.
     * Please Note, that it MUST return null if {@link #getChildren()} is NOT empty.
     *
     * @return  the command to execute if the {@link MenuItem} is selected, or null if {@link #getChildren()} is NOT empty.
     */
    MenuCommand getCommand();

    default int compareTo(MenuItem other) {
        // sort by order first and then by label
        return Comparator
                .comparing(MenuItem::getOrder)
                .thenComparing(MenuItem::getLabel)
                .compare(this, other);
    }
}
