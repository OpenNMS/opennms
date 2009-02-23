package org.opennms.netmgt.provision;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public abstract class BasePolicy<T> implements Policy<T> {
    private final BeanWrapper m_beanWrapper;
    
    public BasePolicy() {
        m_beanWrapper = new BeanWrapperImpl(this);
    }

    public T apply(T entity) {
        return entity;
    }
    
    public String getParameter(String key) {
        return (String)m_beanWrapper.getPropertyValue(key);
    }
    
    public void setParameter(String key, String value) {
        m_beanWrapper.setPropertyValue(key, value);
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
