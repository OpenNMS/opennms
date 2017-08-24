package org.opennms.vaadin.extender;

import com.vaadin.annotations.JavaScript;

/**
 * @deprecated Use the {@link JavaScript} annotation from Vaadin 7 instead.
 */
public class ScriptTag {

    private String m_source;
    private String m_type;
    private String m_contents;

    public ScriptTag() {
    }

    public ScriptTag(final String source, final String type, final String contents) {
        m_source   = source;
        m_type     = type;
        m_contents = contents;
    }

    public String getSource() {
        return m_source;
    }
    
    public void setSource(final String source) {
        m_source = source;
    }
    
    public String getType() {
        return m_type;
    }
    
    public void setType(final String type) {
        m_type = type;
    }
    
    public String getContents() {
        return m_contents;
    }
    
    public void setContents(final String contents) {
        m_contents = contents;
    }
}
