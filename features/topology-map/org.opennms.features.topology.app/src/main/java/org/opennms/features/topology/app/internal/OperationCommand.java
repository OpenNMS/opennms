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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.event.Action;

public class OperationCommand extends Action implements Command  {

    private static final long serialVersionUID = -6018957365232489699L;

    @Override
    public Action getAction() {
        return this;
    }

    Operation m_operation;
    Map<String, String> m_props;
    
    public OperationCommand(String caption, Operation operation, Map<String, String> props) {
        super(caption == null ? props.get(Operation.OPERATION_LABEL) : caption);
        m_operation = operation;
        m_props = props;
    }
    
    public OperationCommand(String caption, String menuLocation, String contextMenuLocation){
    	this(caption, null, getProperties(menuLocation, contextMenuLocation));
    }
    
    public static Map<String, String> getProperties(String menuLocation, String contextMenuLocation){
        Map<String, String> props = new HashMap<String, String>();
        if(menuLocation != null){
            props.put(Operation.OPERATION_MENU_LOCATION, menuLocation);
        }
        
        if(contextMenuLocation != null){
            props.put(Operation.OPERATION_CONTEXT_LOCATION, contextMenuLocation);
        }
        
        return props;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.topology.app.internal.Command#doCommand(java.lang.Object, org.opennms.features.topology.app.internal.SimpleGraphContainer, com.vaadin.ui.Window, org.opennms.features.topology.app.internal.CommandManager)
     */
    @Override
    public void doCommand(List<VertexRef> targets, OperationContext operationContext) {
        m_operation.execute(targets, operationContext);
    }
	
    /* (non-Javadoc)
     * @see org.opennms.features.topology.app.internal.Command#undoCommand()
     */
    @Override
    public void undoCommand() {
        throw new UnsupportedOperationException("The undoCommand is not supported at this time");
        
    }
	
    /* (non-Javadoc)
     * @see org.opennms.features.topology.app.internal.Command#getMenuPosition()
     */
    @Override
    public String getMenuPosition() {
        String menuLocation = m_props.get(Operation.OPERATION_MENU_LOCATION);
        return menuLocation == null ? null : menuLocation.isEmpty() ? getCaption() : menuLocation + "|" + getCaption();
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.topology.app.internal.Command#isAction()
     */
    @Override
    public boolean isAction() {
        String contextLocation = m_props.get(Operation.OPERATION_CONTEXT_LOCATION);
        return contextLocation != null;
    }
    
    @Override
    public String toString() {
        return getCaption();
    }
    
    @Override
    public Operation getOperation() {
        return m_operation;
    }

	@Override
	public String getContextMenuPosition() {
		String contextLocation = m_props.get(Operation.OPERATION_CONTEXT_LOCATION);
		return contextLocation == null ? null : contextLocation.isEmpty() ? getCaption() : contextLocation + "|" + getCaption();
	}

}
