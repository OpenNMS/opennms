package org.opennms.gwt.web.ui.client;

public class UpperCasingLabel {
    
    private String m_text;
    
    public void setText(String string) {
        m_text = string.toUpperCase();
    }
    
    public String getText() {
        return m_text;
    }
    
    
}
