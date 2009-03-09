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
    private Set<String>  m_parameterNames = new TreeSet<String>();
    private final String m_className;
    
    public PluginWrapper(String className) throws ClassNotFoundException {
        m_className = className;
        BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(m_className));
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            m_parameterNames.add(pd.getName());
            Set<String> choices = null;
            if (pd.getPropertyType().isEnum()) {
                choices = new TreeSet<String>();
                for (Object o : pd.getPropertyType().getEnumConstants()) {
                    choices.add(o.toString());
                }
            }
            m_choices.put(pd.getName(), choices);
        }
    }

    public String getClassName() {
        return m_className;
    }
    
    public Set<String> getParameterNames() {
        return m_parameterNames;
    }
    
    public Map<String, Set<String>> getChoices() {
        return m_choices;
    }
}
