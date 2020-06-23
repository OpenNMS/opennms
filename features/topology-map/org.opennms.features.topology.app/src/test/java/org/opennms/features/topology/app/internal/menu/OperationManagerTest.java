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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;
import com.vaadin.ui.MenuBar;

public class OperationManagerTest {
    @Test
    public void submenuAlphabeticalOrderTest() {
        OperationManager cmdManager = new OperationManager();
        cmdManager.addOrUpdateGroupOrder("File", Lists.newArrayList("new", "additions"));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation1?group=new", ""));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation3", ""));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation4", ""));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation2", ""));

        cmdManager.onBind(createTestOperation(), createProperties("File|New", "NewOperation", ""));

        MenuBar menuBar = getMenuBar(cmdManager);

        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());

        List<MenuBar.MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(6, subMenuItems.size());
        assertEquals("Operation1", subMenuItems.get(0).getText());
        assertEquals(Boolean.TRUE, subMenuItems.get(1).isSeparator());
        assertEquals("New", subMenuItems.get(2).getText());
        assertEquals("Operation2", subMenuItems.get(3).getText());
        assertEquals("Operation3", subMenuItems.get(4).getText());
        assertEquals("Operation4", subMenuItems.get(5).getText());

        assertEquals(1, subMenuItems.get(2).getChildren().size());
    }

    @Test
    public void groupingSeparatorTest() {
        OperationManager cmdManager = new OperationManager();
        cmdManager.addOrUpdateGroupOrder("Default", Lists.newArrayList("new", "help", "additions"));

        cmdManager.onBind(createTestOperation(), createProperties("Device", "Operation1?group=additions", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Device", "Operation3?group=additions", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Device", "Operation4?group=additions", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Device", "Operation2?group=additions", ""));
        cmdManager.onBind(createTestOperation(), createProperties(null, "Get Info?group=new", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Device", "NewOperation?group=additions", ""));


        MenuBar menuBar = getMenuBar(cmdManager);

        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());

        List<MenuBar.MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(5, subMenuItems.size());
        assertEquals("NewOperation", subMenuItems.get(0).getText());
        assertEquals("Operation1", subMenuItems.get(1).getText());
        assertEquals("Operation2", subMenuItems.get(2).getText());
        assertEquals("Operation3", subMenuItems.get(3).getText());
        assertEquals("Operation4", subMenuItems.get(4).getText());
    }

    @Test
    public void layoutEditMenuGroupingTest() {
        OperationManager cmdManager = new OperationManager();
        cmdManager.addOrUpdateGroupOrder("Edit", Lists.newArrayList("new", "layout", "additions"));

        cmdManager.onBind(createTestOperation(), createProperties("Edit", "Circle Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "FR Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "ISOM Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "KK Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "Redo Layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "Spring Layout?group=layout", ""));


        MenuBar menuBar = getMenuBar(cmdManager);

        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());

        List<MenuBar.MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(7, subMenuItems.size());
        assertEquals("Circle Layout", subMenuItems.get(0).getText());
        assertEquals("FR Layout", subMenuItems.get(1).getText());
        assertEquals("ISOM Layout", subMenuItems.get(2).getText());
        assertEquals("KK Layout", subMenuItems.get(3).getText());
        assertEquals("Spring Layout", subMenuItems.get(4).getText());
        assertEquals(Boolean.TRUE, subMenuItems.get(5).isSeparator());
        assertEquals("Redo Layout", subMenuItems.get(6).getText());
    }

    @Test
    public void layoutEditMenuGroupingNoGroupTest() {
        OperationManager cmdManager = new OperationManager();
        cmdManager.addOrUpdateGroupOrder("Edit", Lists.newArrayList("new", "middle", "additions"));

        cmdManager.onBind(createTestOperation(), createProperties("Edit", "Circle Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "FR Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "ISOM Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "KK Layout?group=layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "Redo Layout", ""));
        cmdManager.onBind(createTestOperation(), createProperties("Edit", "Spring Layout?group=layout", ""));


        MenuBar menuBar = getMenuBar(cmdManager);

        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());

        List<MenuBar.MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(6, subMenuItems.size());
        assertEquals("Circle Layout", subMenuItems.get(0).getText());
        assertEquals("FR Layout", subMenuItems.get(1).getText());
        assertEquals("ISOM Layout", subMenuItems.get(2).getText());
        assertEquals("KK Layout", subMenuItems.get(3).getText());
        assertEquals("Redo Layout", subMenuItems.get(4).getText());
        assertEquals("Spring Layout", subMenuItems.get(5).getText());
    }

    @Test
    public void submenuGroupOrderAlphabeticallyTest() {
        OperationManager cmdManager = new OperationManager();
        cmdManager.addOrUpdateGroupOrder("File", Lists.newArrayList("new", "help", "additions"));

        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation1", ""));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation3", ""));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation4", ""));
        cmdManager.onBind(createTestOperation(), createProperties("File", "Operation2", ""));

        cmdManager.onBind(createTestOperation(), createProperties("File|New", "NewOperation", ""));

        MenuBar menuBar = getMenuBar(cmdManager);

        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());

        List<MenuBar.MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(5, subMenuItems.size());
        assertEquals("New", subMenuItems.get(0).getText());
        assertEquals("Operation1", subMenuItems.get(1).getText());
        assertEquals("Operation2", subMenuItems.get(2).getText());
        assertEquals("Operation3", subMenuItems.get(3).getText());
        assertEquals("Operation4", subMenuItems.get(4).getText());
    }

    @Test
    public void commandManagerParseConfigTest() {
        Dictionary<String,String> props = new Hashtable<String,String>();
        props.put("toplevelMenuOrder", "File,Edit,View,Additions,Help");
        props.put("submenu.File.groups", "start,new,close,save,print,open,import,additions,end");
        props.put("submenu.Edit.groups", "start,undo,cut,find,add,end,additions");
        props.put("submenu.View.groups", "start,additions,end");
        props.put("submenu.Help.groups", "start,main,tools,updates,end,additions");
        props.put("submenu.Default.groups", "start,main,end,additions");


        Map<String, List<String>> expected = new HashMap<String, List<String>>();
        expected.put("File", Lists.newArrayList("start", "new", "close", "save", "print", "open", "import", "additions", "end"));
        expected.put("Edit", Lists.newArrayList("start", "undo", "cut", "find", "add", "end","additions"));
        expected.put("View", Lists.newArrayList("start", "additions", "end"));
        expected.put("Help", Lists.newArrayList("start", "main", "tools", "updates", "end", "additions"));
        expected.put("Default", Lists.newArrayList("start", "main", "end", "additions"));

        OperationManager cmdManager = new OperationManager();
        cmdManager.updateMenuConfig(props);
        Map<String, List<String>> actual = cmdManager.getMenuOrderConfig();

        assertEquals(expected.get("File"), actual.get("File"));
        assertEquals(expected.get("Edit"), actual.get("Edit"));
        assertEquals(expected.get("View"), actual.get("View"));
        assertEquals(expected.get("Help"), actual.get("Help"));
        assertEquals(expected.get("Default"), actual.get("Default"));
    }

    private static Map<String, String> createProperties(String menuLocation, String label, String contextMenuLocation) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("operation.menuLocation", menuLocation);
        props.put("operation.label", label);
        props.put("operation.contextMenuLocation", contextMenuLocation);
        return props;
    }

    private static Operation createTestOperation() {
        return new Operation() {

            @Override
            public void execute(List<VertexRef> targets, OperationContext operationContext) {

            }

            @Override
            public boolean display(List<VertexRef> targets, OperationContext operationContext) {
                return true;
            }

            @Override
            public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
                return true;
            }

            @Override
            public String getId() {
                return null;
            }
        };
    }
    
    private static TopologyMenuBar getMenuBar(OperationManager operationManager) {
        // Mock all the things
        GraphContainer graphContainerMock = Mockito.mock(GraphContainer.class);
        SelectionManager selectionManagerMock = Mockito.mock(SelectionManager.class);
        Mockito.when(graphContainerMock.getSelectionManager()).thenReturn(selectionManagerMock);
        Mockito.when(selectionManagerMock.getSelectedVertexRefs()).thenReturn(new ArrayList<>());

        // Create menu bar
        TopologyMenuBar topologyMenuBar = new TopologyMenuBar();
        topologyMenuBar.updateMenu(graphContainerMock, null, operationManager);
        return topologyMenuBar;
    }
}
