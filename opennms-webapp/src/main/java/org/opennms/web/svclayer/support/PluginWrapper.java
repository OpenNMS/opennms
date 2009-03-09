package org.opennms.web.svclayer.support;

import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


public class PluginWrapper {
    private Map<String,Set<String>> m_choices = new TreeMap<String,Set<String>>();
    private final String m_className;
    
    public PluginWrapper(Object clazz) {
        m_className = clazz.getClass().getName();
        BeanWrapper wrapper = new BeanWrapperImpl(clazz);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            Set<String> choices = null;
            if (pd.getPropertyType().getClass().isEnum()) {
                choices = new TreeSet<String>();
                for (Object o : pd.getPropertyType().getClass().getEnumConstants()) {
                    choices.add((String)wrapper.convertIfNecessary(o, String.class));
                }
            }
            m_choices.put(pd.getName(), choices);
        }
    }

    public String getClassName() {
        return m_className;
    }
    public Set<String> getKeys() {
        return m_choices.keySet();
    }
    public Set<String> getChoices(String key) {
        return m_choices.get(key);
    }

}
