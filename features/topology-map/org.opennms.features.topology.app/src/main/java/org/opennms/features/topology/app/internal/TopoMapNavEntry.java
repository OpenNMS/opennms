package org.opennms.features.topology.app.internal;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.navigate.ConditionalPageNavEntry;
import org.opennms.web.navigate.DisplayStatus;

public class TopoMapNavEntry implements ConditionalPageNavEntry {
    private String m_name;
    private String m_url;

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    @Override
    public String getUrl() {
        return m_url;
    }

    public void setUrl(final String url) {
        m_url = url;
    }

    @Override
    public DisplayStatus evaluate(final HttpServletRequest request, final Object target) {
        if (target instanceof OnmsNode) {
            final OnmsNode node = (OnmsNode)target;
            if (node != null && node.getId() != null && node.getId() > 0) {
                return DisplayStatus.DISPLAY_LINK;
            }
        }
        return DisplayStatus.NO_DISPLAY;
    }

    @Override public String toString() {
        return "TopoMapNavEntry[url=" + m_url + ",name=" + m_name +"]";
    }
}
