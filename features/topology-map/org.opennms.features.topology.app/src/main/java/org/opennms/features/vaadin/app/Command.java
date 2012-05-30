package org.opennms.features.vaadin.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.Operation;

import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public abstract class Command extends Action implements MenuBar.Command  {

    Operation m_operation;
    Map<String, String> m_props;
    private String m_parentMenu;
    private boolean m_action = false;
    private boolean m_menu = false;
    
    public Command(String caption, Operation operation, Map<String, String> props) {
        super(operation.getLabel());
        m_operation = operation;
        m_props = props;
    }
    
    public boolean appliesToTarget(Object target) {
        return m_operation.enabled(asList(target));
    }
    
    public void menuSelected(MenuItem selectedItem) {
        this.doCommand(null);
    }
    
    public void doCommand(Object target) {
        m_operation.execute(asList(target));
    }
	
	private List<Object> asList(Object target) {
	    if(target instanceof Collection<?>) {
            return new ArrayList<Object>( (Collection<?>) target);
        }
        
        if(target instanceof Object[]) {
            return  Arrays.asList( (Object[]) target);
        }
        
        return Collections.singletonList(target);
        
    }

    public void undoCommand() {
        throw new UnsupportedOperationException("The undoCommand is not supported at this time");
        
    }
	
    public String getMenuPosition() {
        String menuLocation = m_props.get(Operation.OPERATION_MENU_LOCATION);
        return menuLocation == null ? null : menuLocation.isEmpty() ? getCaption() : menuLocation + "|" + getCaption();
    }
    
    public Command setParentMenu(String parentMenu) {
        m_menu = true;
        m_parentMenu = parentMenu;
        return this;
    }
    
    public Command setAction() {
        m_action = true;
        return this;
    }

    public boolean isAction() {
        String contextLocation = m_props.get(Operation.OPERATION_CONTEXT_LOCATION);
        return contextLocation != null;
    }
    
    public String toString() {
        return getCaption();
    }

}
