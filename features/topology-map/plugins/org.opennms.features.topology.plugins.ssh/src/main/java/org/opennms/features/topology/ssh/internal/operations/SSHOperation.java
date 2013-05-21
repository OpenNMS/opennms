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

package org.opennms.features.topology.ssh.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.OperationContext.DisplayLocation;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.ssh.internal.AuthWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class SSHOperation implements Operation {

        @Override
	public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
	    String ipAddr = "";
	    int port = 22;

	    if (targets != null) {
	        for(final VertexRef target : targets) {
	            final Item vertexItem = operationContext.getGraphContainer().getBaseTopology().getVertex(target).getItem();
	            if (vertexItem != null) {
	                final Property ipAddrProperty = vertexItem.getItemProperty("ipAddr");
	                ipAddr = ipAddrProperty == null ? "" : (String) ipAddrProperty.getValue();
	                //Property portProperty = operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port");
	                port = 22; //portProperty == null ? -1 : (Integer) portProperty.getValue();
	            }
	        }
	    }
	    operationContext.getMainWindow().addWindow(new AuthWindow(ipAddr, port));
	    return null;
	}

        @Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
	    if (operationContext.getDisplayLocation() == DisplayLocation.MENUBAR) {
	    	return true;
	    } else if(targets != null && targets.size() > 0 && targets.get(0) != null) {
	        return true;
	    } else {
	        return false;
	    }
	    
	}

        @Override
	public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
	    if (targets == null || targets.size() < 2) return true;
	    return false;
	}

        @Override
	public String getId() {
	    return "SSH";
	}

}
