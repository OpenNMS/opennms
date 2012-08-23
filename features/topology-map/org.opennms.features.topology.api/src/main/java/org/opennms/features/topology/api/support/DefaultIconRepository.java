package org.opennms.features.topology.api.support;

import java.util.Map;

import org.opennms.features.topology.api.IconRepository;

public class DefaultIconRepository implements IconRepository {

    private Map<String, String> m_iconMap;

    @Override
    public boolean contains(String type) {
        return m_iconMap.containsKey(type);
    }
    
    public void setIconMap(Map<String, String> icons) {
        m_iconMap = icons;
    }

    @Override
    public String getIconUrl(String type) {
        return m_iconMap.get(type);
    }

}
