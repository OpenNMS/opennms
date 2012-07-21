package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.event.Action;

public class OperationCommand extends Action implements Command  {

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
     * @see org.opennms.features.topology.app.internal.Command#appliesToTarget(java.lang.Object, org.opennms.features.topology.app.internal.SimpleGraphContainer)
     */
    @Override
    public boolean appliesToTarget(Object target, OperationContext operationContext) {
        return m_operation.enabled(asList(target), operationContext);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.topology.app.internal.Command#doCommand(java.lang.Object, org.opennms.features.topology.app.internal.SimpleGraphContainer, com.vaadin.ui.Window, org.opennms.features.topology.app.internal.CommandManager)
     */
    @Override
    public void doCommand(Object target, OperationContext operationContext) {
        m_operation.execute(asList(target), operationContext);
    }
	
	private List<Object> asList(Object target) {
	    if(target != null) {
    	    if(target instanceof Collection<?>) {
                return new ArrayList<Object>( (Collection<?>) target);
            }
            
            if(target instanceof Object[]) {
                return  Arrays.asList( (Object[]) target);
            }
            
            return Collections.singletonList(target);
	    }else {
	        return Collections.emptyList();
	    }
        
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
    
    public String toString() {
        return getCaption();
    }
    
    public Operation getOperation() {
        return m_operation;
    }

}
