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

package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
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
			public void doCommand(List<VertexRef> targets, OperationContext operationContext) {
			}

			@Override
			public void undoCommand() {
			}

			@Override
			public String getMenuPosition() {
				return null;
			}

			@Override
			public String getContextMenuPosition() {
				return null;
			}

			@Override
			public boolean isAction() {
				return false;
			}

			@Override
			public Action getAction() {
				return null;
			}

			@Override
			public Operation getOperation() {
				return new Operation() {

                    @Override
                    public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
                        return null;
                    }

                    @Override
                    public boolean display(List<VertexRef> targets,
                            OperationContext operationContext) {
                        return false;
                    }

                    @Override
                    public boolean enabled(List<VertexRef> targets,
                            OperationContext operationContext) {
                        return false;
                    }

                    @Override
                    public String getId() {
                        return null;
                    }};
			}
			
		};
	}

}
