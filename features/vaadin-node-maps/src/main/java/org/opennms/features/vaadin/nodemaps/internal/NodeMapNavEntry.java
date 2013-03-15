package org.opennms.features.vaadin.nodemaps.internal;

import javax.servlet.http.HttpServletRequest;

import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.PageNavEntry;

public class NodeMapNavEntry implements PageNavEntry {
    private String m_name;
    private String m_url;

    @Override public DisplayStatus evaluate(final HttpServletRequest request) {
        return null;
    }

    @Override public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    @Override public String getUrl() {
        return m_url;
    }
    
    public void setUrl(final String url) {
        m_url = url;
    }
    
    @Override public String toString() {
        return "NodeMapNavEntry[url=" + m_url + ",name=" + m_name +"]";
    }
}
