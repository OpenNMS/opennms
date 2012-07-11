package org.opennms;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.data.Item;

public class SSHOperation implements Operation {

    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        
        for(Object target : targets) {
            String host = (String) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("host").getValue();
            int port = (Integer) operationContext.getGraphContainer().getVertexItem(target).getItemProperty("port").getValue();
        }
        String host = "";
        int port = 0;
        AuthWindow authWindow = new AuthWindow(this, operationContext.getMainWindow(), host, port);
        
        return null;
    }

    public boolean display(List<Object> targets, OperationContext operationContext) {
        
        return false;
    }

    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        if(targets.size() == 1) {
            return true;
        }
        for(Object target : targets) {
            Object itemId = target;
            Item vertexItem = operationContext.getGraphContainer().getVertexItem(itemId);
            if(vertexItem.getItemProperty("host").getValue() != null) {
                
            }
        }
        return false;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

}
