/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class TestMenuBarBuilder {

    @Test
    public void createMenuTest() {
        MenuBarBuilder builder = new MenuBarBuilder();
        builder.setTopLevelMenuOrder(Arrays.asList("File", "Edit", "View"));
        builder.addMenuCommand(null, "File");
        builder.addMenuCommand(null, "View");
        builder.addMenuCommand(null, "Edit");
        builder.addMenuCommand(createEmpyCommand(), "File|Test");
       
        
        MenuBar menuBar = builder.get();
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(3, menuItems.size());
        assertEquals("File", menuItems.get(0).getText());
        assertEquals("Edit", menuItems.get(1).getText());
        assertEquals("View", menuItems.get(2).getText());
    }
    
    @Test
    public void createTopLevelMenuWithAdditionsTest() {
        MenuBarBuilder builder = new MenuBarBuilder();
        builder.setTopLevelMenuOrder(Arrays.asList("File", "Edit", "View", "Additions", "Help"));
        builder.addMenuCommand(null, "Edit");
        builder.addMenuCommand(null, "Test2");
        builder.addMenuCommand(null, "File");
        builder.addMenuCommand(null, "Test1");
        builder.addMenuCommand(null, "Help");
        builder.addMenuCommand(null, "View");
        
        MenuBar menuBar = builder.get();
        List<MenuItem> menuItems = menuBar.getItems();
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
        MenuBarBuilder builder = new MenuBarBuilder();
        builder.addMenuCommand(null, "Edit");
        builder.addMenuCommand(null, "Test2");
        builder.addMenuCommand(null, "File");
        builder.addMenuCommand(null, "Test1");
        builder.addMenuCommand(null, "Help");
        builder.addMenuCommand(null, "View");
        
        MenuBar menuBar = builder.get();
        List<MenuItem> menuItems = menuBar.getItems();
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
        MenuBarBuilder builder = new MenuBarBuilder();
        builder.setTopLevelMenuOrder(Arrays.asList("File", "Edit", "View", "Help"));
        builder.addMenuCommand(null, "Edit");
        builder.addMenuCommand(null, "Test2");
        builder.addMenuCommand(null, "File");
        builder.addMenuCommand(null, "Test1");
        builder.addMenuCommand(null, "Help");
        builder.addMenuCommand(null, "View");
        
        MenuBar menuBar = builder.get();
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(6, menuItems.size());
        assertEquals("File", menuItems.get(0).getText());
        assertEquals("Edit", menuItems.get(1).getText());
        assertEquals("View", menuItems.get(2).getText());
        assertEquals("Help", menuItems.get(3).getText());
        assertEquals("Test1", menuItems.get(4).getText());
        assertEquals("Test2", menuItems.get(5).getText());
    }
    
    @Test
    public void submenuAlphabeticalOrderTest() {
        CommandManager cmdManager = new CommandManager();
        cmdManager.addOrUpdateGroupOrder("File", Arrays.asList("new", "additions"));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation1?group=new", ""));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation3", ""));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation4", ""));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation2", ""));
        
        cmdManager.onBind(getTestOperation(), getProps("File|New", "NewOperation", ""));
        
        MenuBar menuBar = cmdManager.getMenuBar(null, null);
        
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());
        
        List<MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(6, subMenuItems.size());
        assertEquals("New", subMenuItems.get(0).getText());
        assertEquals("Operation1", subMenuItems.get(1).getText());
        assertEquals("", subMenuItems.get(2).getText());
        assertEquals("Operation2", subMenuItems.get(3).getText());
        assertEquals("Operation3", subMenuItems.get(4).getText());
        assertEquals("Operation4", subMenuItems.get(5).getText());
        
    }
    
    @Test
    public void groupingSeparatorTest() {
        CommandManager cmdManager = new CommandManager();
        cmdManager.addOrUpdateGroupOrder("Default", Arrays.asList("new", "help", "additions"));
        
        cmdManager.onBind(getTestOperation(), getProps("Device", "Operation1?group=additions", ""));
        cmdManager.onBind(getTestOperation(), getProps("Device", "Operation3?group=additions", ""));
        cmdManager.onBind(getTestOperation(), getProps("Device", "Operation4?group=additions", ""));
        cmdManager.onBind(getTestOperation(), getProps("Device", "Operation2?group=additions", ""));
        cmdManager.onBind(getTestOperation(), getProps(null, "Get Info?group=new", ""));
        cmdManager.onBind(getTestOperation(), getProps("Device", "NewOperation?group=additions", ""));
        
        
        MenuBar menuBar = cmdManager.getMenuBar(null, null);
        
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());
        
        List<MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(5, subMenuItems.size());
        assertEquals("NewOperation", subMenuItems.get(0).getText());
        assertEquals("Operation1", subMenuItems.get(1).getText());
        assertEquals("Operation2", subMenuItems.get(2).getText());
        assertEquals("Operation3", subMenuItems.get(3).getText());
        assertEquals("Operation4", subMenuItems.get(4).getText());
    }
    
    @Test
    public void layoutEditMenuGroupingTest() {
        CommandManager cmdManager = new CommandManager();
        cmdManager.addOrUpdateGroupOrder("Edit", Arrays.asList("new", "layout", "additions"));
        
        cmdManager.onBind(getTestOperation(), getProps("Edit", "Circle Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "FR Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "ISOM Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "KK Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "Redo Layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "Spring Layout?group=layout", ""));
        
        
        MenuBar menuBar = cmdManager.getMenuBar(null, null);
        
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());
        
        List<MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(7, subMenuItems.size());
        assertEquals("Circle Layout", subMenuItems.get(0).getText());
        assertEquals("FR Layout", subMenuItems.get(1).getText());
        assertEquals("ISOM Layout", subMenuItems.get(2).getText());
        assertEquals("KK Layout", subMenuItems.get(3).getText());
        assertEquals("Spring Layout", subMenuItems.get(4).getText());
        assertEquals("", subMenuItems.get(5).getText());
        assertEquals("Redo Layout", subMenuItems.get(6).getText());
    }
    
    @Test
    public void layoutEditMenuGroupingNoGroupTest() {
        CommandManager cmdManager = new CommandManager();
        cmdManager.addOrUpdateGroupOrder("Edit", Arrays.asList("new", "middle", "additions"));
        
        cmdManager.onBind(getTestOperation(), getProps("Edit", "Circle Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "FR Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "ISOM Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "KK Layout?group=layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "Redo Layout", ""));
        cmdManager.onBind(getTestOperation(), getProps("Edit", "Spring Layout?group=layout", ""));
        
        
        MenuBar menuBar = cmdManager.getMenuBar(null, null);
        
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());
        
        List<MenuItem> subMenuItems = menuItems.get(0).getChildren();
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
        CommandManager cmdManager = new CommandManager();
        cmdManager.addOrUpdateGroupOrder("File", Arrays.asList("new", "help", "additions"));
        
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation1", ""));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation3", ""));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation4", ""));
        cmdManager.onBind(getTestOperation(), getProps("File", "Operation2", ""));
        
        cmdManager.onBind(getTestOperation(), getProps("File|New", "NewOperation", ""));
        
        MenuBar menuBar = cmdManager.getMenuBar(null, null);
        
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());
        
        List<MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(5, subMenuItems.size());
        assertEquals("New", subMenuItems.get(0).getText());
        assertEquals("Operation1", subMenuItems.get(1).getText());
        assertEquals("Operation2", subMenuItems.get(2).getText());
        assertEquals("Operation3", subMenuItems.get(3).getText());
        assertEquals("Operation4", subMenuItems.get(4).getText());
    }
    
    @Test
    @Ignore
    public void submenuCheckedMenuItemTest() {
        CommandManager cmdManager = new CommandManager();
        cmdManager.addOrUpdateGroupOrder("File", Arrays.asList("new", "help", "additions"));
        
        cmdManager.onBind(getCheckedTestOperation(), getProps("File", "Operation1", ""));
        
        MenuBar menuBar = cmdManager.getMenuBar(null, null);
        
        List<MenuItem> menuItems = menuBar.getItems();
        assertEquals(1, menuItems.size());
        
        List<MenuItem> subMenuItems = menuItems.get(0).getChildren();
        assertEquals(1, subMenuItems.size());
        MenuItem menuItem = subMenuItems.get(0);
        assertEquals("Operation1", menuItem.getText());
        assertTrue(menuItem.isCheckable());
    }
    
    private CheckedOperation getCheckedTestOperation() {
        return new CheckedOperation() {

            @Override
            public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean display(List<VertexRef> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public String getId() {
                // TODO Auto-generated method stub
                return null;
            }

			@Override
			public boolean isChecked(List<VertexRef> targets,
					OperationContext operationContext) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void applyHistory(GraphContainer context,
					Map<String, String> settings) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Map<String, String> createHistory(GraphContainer context) {
				// TODO Auto-generated method stub
				return null;
			}};
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
        expected.put("File", Arrays.asList("start", "new", "close", "save", "print", "open", "import", "additions", "end"));
        expected.put("Edit", Arrays.asList("start", "undo", "cut", "find", "add", "end","additions"));
        expected.put("View", Arrays.asList("start", "additions", "end"));
        expected.put("Help", Arrays.asList("start", "main", "tools", "updates", "end", "additions"));
        expected.put("Default", Arrays.asList("start", "main", "end", "additions"));
        
        CommandManager cmdManager = new CommandManager();
        cmdManager.updateMenuConfig(props);
        Map<String, List<String>> actual = cmdManager.getMenuOrderConfig();
        
        assertEquals(expected.get("File"), actual.get("File"));
        assertEquals(expected.get("Edit"), actual.get("Edit"));
        assertEquals(expected.get("View"), actual.get("View"));
        assertEquals(expected.get("Help"), actual.get("Help"));
        assertEquals(expected.get("Default"), actual.get("Default"));
        
        
    }
   

    private Map<String, String> getProps(String menuLocation, String label, String contextMenuLocation) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("operation.menuLocation", menuLocation);
        props.put("operation.label", label);
        props.put("operation.contextMenuLocation", contextMenuLocation);
        return props;
    }

    private Operation getTestOperation() {
        return new Operation() {

            @Override
            public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean display(List<VertexRef> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public String getId() {
                // TODO Auto-generated method stub
                return null;
            }};
    }

    private Command createEmpyCommand() {
        
        return new Command() {

            @Override
            public void menuSelected(MenuItem selectedItem) {
                
            }
        };
    }
    
    private Command menuCommand = new Command() {
        @Override
        public void menuSelected(MenuItem selectedItem) {
            
        }
    };

}
