package org.opennms.netmgt.provision;

import java.util.Map;
import java.util.TreeMap;

public abstract class BasePolicy<T> implements Policy<T> {
    private final Map<String,String> m_parameters = new TreeMap<String,String>();
    
    public T apply(T entity) {
        return entity;
    }
    
    public String getParameter(String key) {
        return m_parameters.get(key);
    }
    
    public void setParameter(String key, String value) {
        m_parameters.put(key, value);
    }

    protected boolean match(String s, String matcher) {
        if (s == null) {
            return false;
        }
        if (matcher.startsWith("~")) {
            matcher = matcher.replaceFirst("~", "");
            return s.matches(matcher);
        } else {
            return s.equals(matcher);
        }
    }
}
