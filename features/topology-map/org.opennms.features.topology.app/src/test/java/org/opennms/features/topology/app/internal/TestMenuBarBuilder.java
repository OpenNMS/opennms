package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class TestMenuBarBuilder {

    private MenuBar m_menubar = new MenuBar();
    
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
    public void createTopLevelMenuWithAdditionalTest() {
        MenuBarBuilder builder = new MenuBarBuilder();
        builder.setTopLevelMenuOrder(Arrays.asList("File", "Edit", "View", "Additional", "Help"));
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
    public void submenuGroupOrderAlphabeticallyTest() {
        CommandManager cmdManager = new CommandManager();
        cmdManager.addGroupOrder("File", Arrays.asList("new", "help", "additional"));
        
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
            public Undoer execute(List<Object> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean display(List<Object> targets, OperationContext operationContext) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean enabled(List<Object> targets, OperationContext operationContext) {
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
        public void menuSelected(MenuItem selectedItem) {
            
        }
    };

}
