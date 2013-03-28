package org.opennms.features.vaadin.nodemaps.internal;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.navigate.ConditionalPageNavEntry;
import org.opennms.web.navigate.DisplayStatus;

public class NodeMapNavEntry implements ConditionalPageNavEntry {
    private String m_name;
    private String m_url;

    @Override public DisplayStatus evaluate(final HttpServletRequest request, final Object target) {
        if (target instanceof OnmsNode) {
            final OnmsNode node = (OnmsNode)target;
            if (node.getAssetRecord() != null && node.getAssetRecord().getGeolocation() != null) {
                final OnmsGeolocation geolocation = node.getAssetRecord().getGeolocation();
                if (geolocation.getLongitude() != null && geolocation.getLatitude() != null) {
                    return DisplayStatus.DISPLAY_LINK;
                }
            }
        }
        return DisplayStatus.NO_DISPLAY;
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
