package org.opennms.web.controller.ncs;

import javax.servlet.http.HttpServletRequest;

import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.PageNavEntry;

public class NCSAlarmListNavItem implements PageNavEntry {
    
    private String m_name;
    private String m_url;
    
    public String getName() {
        return m_name;
    }

    public String getUrl() {
        return m_url;
    }

    public DisplayStatus evaluate(HttpServletRequest request) {
        return null;
    }

    public void setName(String name) {
        m_name = name;
    }

    public void setUrl(String url) {
        m_url = url;
    }

}
