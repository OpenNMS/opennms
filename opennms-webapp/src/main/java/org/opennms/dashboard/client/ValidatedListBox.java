/**
 * 
 */
package org.opennms.dashboard.client;


import com.google.gwt.user.client.ui.ListBox;

public class ValidatedListBox extends ListBox {
    /**
     * 
     */
    private Dashlet m_dashlet;
    private boolean m_allowWrapAround = true;
    private ValidatedListBox m_parent = null;
    private DirectionalChangeListener m_directionalChangeListener = null;

    public ValidatedListBox(Dashlet dashlet) {
        super();
        m_dashlet = dashlet;
    }
    
    public void setParent(ValidatedListBox parent) {
        m_parent = parent;
    }
    
    public void setDirectionalChangeListener(DirectionalChangeListener listener) {
        m_directionalChangeListener = listener;
    }
    
    public String getSelectedValue() {
        int index = getSelectedIndex();
        if (index < 0 || index >= getItemCount()) {
            m_dashlet.error("Error: list box index " + index + " is out of range");
            return null;
        }
        
        return getValue(index);
    }

    public String getRelativeSelectedValue(int offset) {
        int relativeIndex = getSelectedIndex() + offset;
        if (relativeIndex < 0 || relativeIndex >= getItemCount()) {
            return null;
        }
        
        return getValue(relativeIndex);
    }
    
    public void adjustSelectedValue(int direction) {
        int newPrefabIndex = getSelectedIndex() + direction;
        if (newPrefabIndex < 0) {
            if (m_parent != null) {
                m_parent.adjustSelectedValue(-1);
                return;
            } else {
                if (m_allowWrapAround) {
                    newPrefabIndex = getItemCount() -1;
                } else {
                    return;
                }
            }
        } else if (newPrefabIndex >= getItemCount()) {
            if (m_parent != null) {
                m_parent.adjustSelectedValue(1);
                return;
            } else {
                if (m_allowWrapAround) {
                    newPrefabIndex = 0;
                } else {
                    return;
                }
            }
        }
        
        setSelectedIndex(newPrefabIndex);
        
        if (m_directionalChangeListener != null) {
            m_directionalChangeListener.onChange(this, direction);
        }
    }
}