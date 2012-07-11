package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
