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

package org.opennms.features.topology.netutils.internal.operations;

import java.net.URL;
import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.ResourceGraphsWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class ResourceGraphsOperation implements Operation {

    private String m_resourceGraphListURL;
    private String m_resourceGraphNodeURL;

    public boolean display(final List<Object> targets, final OperationContext operationContext) {
        return true;
    }

    public boolean enabled(final List<Object> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2) {
            return true;
        }
        return false;
    }

    public Undoer execute(final List<Object> targets, final OperationContext operationContext) {
        String label = "";
        int nodeID = -1;

        try {
            if (targets != null) {
                for (final Object target : targets) {
                    final Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
                    if (vertexItem != null) {
                        final Property labelProperty = vertexItem.getItemProperty("label");
                        label = labelProperty == null ? "" : (String) labelProperty.getValue();
                        final Property nodeIDProperty = vertexItem.getItemProperty("nodeID");
                        nodeID = nodeIDProperty == null ? -1 : (Integer) nodeIDProperty.getValue();
                    }
                }
            }
            final Node node = new Node(nodeID, null, label);

            final URL baseURL = operationContext.getMainWindow().getURL();

            final URL nodeURL;

            if (node.getNodeID() >= 0) {
                nodeURL = new URL(baseURL, getResourceGraphNodeURL() + "[" + node.getNodeID() + "]");
            } else {
                nodeURL = new URL(baseURL, getResourceGraphListURL());
            }

            operationContext.getMainWindow().addWindow(new ResourceGraphsWindow(node, nodeURL));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getId() {
        return "contextResourceGraphs";
    }

    public String getResourceGraphListURL() {
        return m_resourceGraphListURL;
    }

    public void setResourceGraphListURL(String resourceGraphListURL) {
        this.m_resourceGraphListURL = resourceGraphListURL;
    }
    
    public String getResourceGraphNodeURL() {
        return m_resourceGraphNodeURL;
    }
    
    public void setResourceGraphNodeURL(final String resourceGraphNodeURL) {
        m_resourceGraphNodeURL = resourceGraphNodeURL;
    }

}
