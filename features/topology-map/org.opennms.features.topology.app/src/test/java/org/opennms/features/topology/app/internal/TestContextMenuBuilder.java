package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;

import com.vaadin.event.Action;

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
        List<TopoContextMenuItem> contextMenuItems = m_cMenu.getItems();
        assertEquals(1, contextMenuItems.size());
        assertEquals("Layout", contextMenuItems.get(0).getName());
        assertNull(contextMenuItems.get(0).getOperation());
        List<TopoContextMenuItem> subMenuItems = contextMenuItems.get(0).getChildren();
        assertEquals(1, subMenuItems.size());
        TopoContextMenuItem submenuItem = subMenuItems.get(0);
        assertEquals("Test", submenuItem.getName());
        assertNotNull(submenuItem.getOperation());
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
				return new Operation() {

                    @Override
                    public Undoer execute(List<Object> targets,
                            OperationContext operationContext) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public boolean display(List<Object> targets,
                            OperationContext operationContext) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean enabled(List<Object> targets,
                            OperationContext operationContext) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public String getId() {
                        // TODO Auto-generated method stub
                        return null;
                    }};
			}
			
		};
	}

}
