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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;
import org.opennms.features.topology.app.internal.TestOperationContext;

import com.google.common.collect.Lists;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;

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
        TopologyContextMenu contextMenu = new TopologyContextMenu(mock(UI.class), menubar);

        final List<MenuBar.MenuItem> contextMenuItems = contextMenu.getItems();
        assertEquals(1, contextMenuItems.size());
        assertEquals("Layout", contextMenuItems.get(0).getText());

        final List<MenuBar.MenuItem> subMenuItems = contextMenuItems.get(0).getChildren();
        assertEquals(1, subMenuItems.size());
        final MenuBar.MenuItem submenuItem = subMenuItems.get(0);
        assertEquals("Test", submenuItem.getText());
    }

    private static TestOperationContext createTestOperationContext() {
        return new TestOperationContext(null);
    }

    private static MenuItem createEmptyMenuItem(String label) {
        return new SimpleMenuItem(label);
    }
}
