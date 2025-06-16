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

/**
 * Vaadin's API for the menus is not ideal.
 *
 * In order to create the menus for the {@link com.vaadin.contextmenu.ContextMenu} and the {@link com.vaadin.ui.MenuBar}
 * it is required to add menu items to the root element (e.g. {@link com.vaadin.ui.MenuBar} and
 * the items (e.g. {@link com.vaadin.ui.MenuBar.MenuItem}) itself. By default they are not compatible.
 * This interface allows to encapsulate the "add item logic" in order to allow the same logic to create the menu items.
 *
 * @param <T> The type of the created Menu Item (e.g. {@link com.vaadin.contextmenu.MenuItem , or {@link com.vaadin.ui.MenuBar.MenuItem}}
 * @author mvrueden
 */
interface ItemAddBehaviour<T> {
    T addItem();
}
