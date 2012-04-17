package org.opennms.features.vaadin.app;

import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public abstract class Command extends Action implements MenuBar.Command, Action.Handler {
	
    public Command(String caption) {
        super(caption);
    }

    private final static Action[] EMPTY_ACTIONS = new Action[0];
    private String m_parentMenu;
    private boolean m_action = false;
    private boolean m_menu = false;
    
    
    public abstract boolean appliesToTarget(Object target);
    
    public Action[] getActions(Object target, Object sender) {
        if(m_action && appliesToTarget(target)) {
            return new Action[] {this};
        }else {
            return EMPTY_ACTIONS;
        }
        
    }

    public void handleAction(Action action, Object sender, Object target) {
        if(action == this) {
            this.doCommand(target);
        }
    }

    public void menuSelected(MenuItem selectedItem) {
        this.doCommand(null);
    }
    
    public abstract void doCommand(Object target);
	
	public abstract void undoCommand();
	
    public String getMenuPosition() {
        return !m_menu ? null : m_parentMenu == null ? getCaption() : m_parentMenu + "|" + getCaption();
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

}
