package org.opennms.gwt.web.ui.client;

/**
 * <p>UpperCasingLabel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class UpperCasingLabel {
    
    private String m_text;
    
    /**
     * <p>setText</p>
     *
     * @param string a {@link java.lang.String} object.
     */
    public void setText(String string) {
        m_text = string.toUpperCase();
    }
    
    /**
     * <p>getText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getText() {
        return m_text;
    }
    
    
}
