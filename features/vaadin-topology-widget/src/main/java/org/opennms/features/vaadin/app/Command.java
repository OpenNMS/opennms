package org.opennms.features.vaadin.app;

import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public abstract class Command extends Action implements MenuBar.Command {
	
    public Command(String caption) {
        super(caption);
    }

    private String m_parentMenu;
    private boolean m_action = false;
    private boolean m_menu = false;
    
    public boolean appliesToTarget(Object target) {
    	return true;
    }
    
    public void menuSelected(MenuItem selectedItem) {
        this.doCommand(null);
    }
    
    public abstract void doCommand(Object target);
	
	public void undoCommand() {}
	
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

    public boolean isAction() {
        return m_action;
    }
    
    public String toString() {
        return getCaption();
    }

}
