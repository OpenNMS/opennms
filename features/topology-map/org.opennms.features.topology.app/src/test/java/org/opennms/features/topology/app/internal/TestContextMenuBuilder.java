package org.opennms.features.topology.app.internal;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public class TestContextMenuBuilder {

	TopoContextMenu m_cMenu;
	
	@Before
	public void setUp() throws Exception {
		m_cMenu = null;
	}
    
    @Test
    public void createContextMenuTest() {
        ContextMenuBuilder builder = new ContextMenuBuilder();
        builder.addMenuCommand(createEmpyCommand(), "Layout|Test");
       
        
        m_cMenu = builder.get();
        List<ContextMenuItem> contextMenuItems = m_cMenu.getItems();
        assertEquals(1, contextMenuItems.size());
        assertEquals("Layout", contextMenuItems.get(0).getName());
//        List<ContextMenuItem> subMenuItems = contextMenuItems.get(0).getChildren();
//        assertEquals(1, subMenuItems.size());
//        assertEquals("Test", subMenuItems.get(0).getName());
        //assertEquals("", contextMenuItems.get(0).)
    }

	private Command createEmpyCommand() {
		return new Command() {

			@Override
			public boolean appliesToTarget(Object target,
					OperationContext operationContext) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void doCommand(Object target,
					OperationContext operationContext) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void undoCommand() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public String getMenuPosition() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getContextMenuPosition() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isAction() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Action getAction() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Operation getOperation() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

}
