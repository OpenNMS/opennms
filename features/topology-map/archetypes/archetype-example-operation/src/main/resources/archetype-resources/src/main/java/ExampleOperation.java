#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

public class ExampleOperation implements Operation {

    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        operationContext.getMainWindow().showNotification("This is an Example Operation, there isn't much to it");
        return null;
    }

    public boolean display(List<Object> targets, OperationContext operationContext) {
        return false;
    }

    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    public String getId() {
        return null;
    }

}
