package org.opennms.features.topology.netutils.internal.operations;

import java.net.URL;
import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.Node;
import org.opennms.features.topology.netutils.internal.NodeInfoWindow;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class NodeInfoOperation implements Operation {
    private String m_nodePageURL;
    private String m_nodeListURL;

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
                nodeURL = new URL(baseURL, getNodePageURL() + "[" + node.getNodeID() + "]");
            } else {
                nodeURL = new URL(baseURL, getNodeListURL());
            }

            operationContext.getMainWindow().addWindow(new NodeInfoWindow(node, nodeURL));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getId() {
        return "contextNodeInfo";
    }
    
    public String getNodePageURL() {
        return m_nodePageURL;
    }

    public void setNodePageURL(final String nodePageURL) {
        m_nodePageURL = nodePageURL;
    }

    public String getNodeListURL() {
        return m_nodeListURL;
    }

    public void setNodeListURL(String nodeListURL) {
        this.m_nodeListURL = nodeListURL;
    }

}
