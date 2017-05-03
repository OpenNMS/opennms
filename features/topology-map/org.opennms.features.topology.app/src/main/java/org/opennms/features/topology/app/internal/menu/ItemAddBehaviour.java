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

package org.opennms.features.topology.app.internal.menu;

import org.vaadin.peter.contextmenu.ContextMenu;

/**
 * Vaadin's API for the menus is not ideal.
 * In order to create the menus for the {@link ContextMenu} and the {@link com.vaadin.ui.MenuBar}
 * it is required to add menu items to the root element (e.g. {@link com.vaadin.ui.MenuBar} and
 * the items (e.g. {@link com.vaadin.ui.MenuBar.MenuItem}) itself. By default they are not compatible.
 * This interface allows to encapsulate the "add item logic" in order to allow the same logic to create the menu items.
 *
 * @param <T> The type of the created Menu Item (e.g. {@link ContextMenu.ContextMenuItem , or {@link com.vaadin.ui.MenuBar.MenuItem}}
 * @author mvrueden
 */
interface ItemAddBehaviour<T> {
    T addItem();
}
