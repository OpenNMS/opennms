/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.menu;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.opennms.features.topology.app.internal.TestOperationContext;
import org.vaadin.peter.contextmenu.client.ContextMenuState;

import com.google.common.collect.Lists;
import com.vaadin.ui.MenuBar;

public class MenuBuilderTest {

    @Test
    public void createMenuTest() {
        MenuBuilder builder = new MenuBuilder();
        builder.setTopLevelMenuOrder(Lists.newArrayList("File", "Edit", "View"));
        builder.createPath("File");
        builder.createPath("View");
        builder.createPath("Edit");
        builder.addMenuItem(createEmptyMenuItem("Test"), "File");

        MenuBar menuBar = builder.build(Lists.newArrayList(), createTestOperationContext());
        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(3, menuItems.size());
        assertEquals("File", menuItems.get(0).getText());
        assertEquals("Edit", menuItems.get(1).getText());
        assertEquals("View", menuItems.get(2).getText());
    }

    @Test
    public void createTopLevelMenuWithAdditionsTest() {
        MenuBuilder builder = new MenuBuilder();
        builder.setTopLevelMenuOrder(Lists.newArrayList("File", "Edit", "View", "Additions", "Help"));
        builder.createPath("Edit");
        builder.createPath("Test2");
        builder.createPath("File");
        builder.createPath("Test1");
        builder.createPath("Help");
        builder.createPath("View");

        MenuBar menuBar = builder.build(Lists.newArrayList(), createTestOperationContext());
        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(6, menuItems.size());
        assertEquals("File", menuItems.get(0).getText());
        assertEquals("Edit", menuItems.get(1).getText());
        assertEquals("View", menuItems.get(2).getText());
        assertEquals("Test1", menuItems.get(3).getText());
        assertEquals("Test2", menuItems.get(4).getText());
        assertEquals("Help", menuItems.get(5).getText());
    }

    @Test
    public void menuItemNoOrderTest() {
        MenuBuilder builder = new MenuBuilder();
        builder.createPath("Edit");
        builder.createPath("Test2");
        builder.createPath("File");
        builder.createPath("Test1");
        builder.createPath("Help");
        builder.createPath("View");

        MenuBar menuBar = builder.build(Lists.newArrayList(), createTestOperationContext());
        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(6, menuItems.size());
        assertEquals("Edit", menuItems.get(0).getText());
        assertEquals("File", menuItems.get(1).getText());
        assertEquals("Help", menuItems.get(2).getText());
        assertEquals("Test1", menuItems.get(3).getText());
        assertEquals("Test2", menuItems.get(4).getText());
        assertEquals("View", menuItems.get(5).getText());
    }

    @Test
    public void menuOrderNoAdditionalTest() {
        MenuBuilder builder = new MenuBuilder();
        builder.setTopLevelMenuOrder(Lists.newArrayList("File", "Edit", "View", "Help"));
        builder.createPath("Edit");
        builder.createPath("Test2");
        builder.createPath("File");
        builder.createPath("Test1");
        builder.createPath("Help");
        builder.createPath("View");

        MenuBar menuBar = builder.build(Lists.newArrayList(), createTestOperationContext());
        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(6, menuItems.size());
        assertEquals("File", menuItems.get(0).getText());
        assertEquals("Edit", menuItems.get(1).getText());
        assertEquals("View", menuItems.get(2).getText());
        assertEquals("Help", menuItems.get(3).getText());
        assertEquals("Test1", menuItems.get(4).getText());
        assertEquals("Test2", menuItems.get(5).getText());
    }

    @Test
    public void createContextMenuTest() {
        MenuBuilder builder = new MenuBuilder();
        builder.addMenuItem(createEmptyMenuItem("Test"), "Layout");

        MenuBar menubar = builder.build(Lists.newArrayList(), new TestOperationContext(null));
        TopologyContextMenu contextMenu = new TopologyContextMenu(menubar);

        List<ContextMenuState.ContextMenuItemState> contextMenuItems = contextMenu.getItems();
        assertEquals(1, contextMenuItems.size());
        assertEquals("Layout", contextMenuItems.get(0).caption);

        List<ContextMenuState.ContextMenuItemState> subMenuItems = contextMenuItems.get(0).getChildren();
        assertEquals(1, subMenuItems.size());
        ContextMenuState.ContextMenuItemState submenuItem = subMenuItems.get(0);
        assertEquals("Test", submenuItem.caption);
    }

    private static TestOperationContext createTestOperationContext() {
        return new TestOperationContext(null);
    }

    private static MenuItem createEmptyMenuItem(String label) {
        return new SimpleMenuItem(label);
    }
}
