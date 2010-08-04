package org.opennms.features.poller.remote.gwt.client.data;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public class TagFilter implements LocationFilter {
    
    
    private String m_selectedTag = null;

    public void setSelectedTag(String selectedTag) {
        m_selectedTag = selectedTag;
    }

    public String getSelectedTag() {
        return m_selectedTag;
    }

    public boolean matches(final LocationInfo location) {
        return getSelectedTag() == null ? true : location.hasTag(getSelectedTag());
    }
}
