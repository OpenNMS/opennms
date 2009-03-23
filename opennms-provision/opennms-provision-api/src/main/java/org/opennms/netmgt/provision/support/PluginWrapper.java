 package org.opennms.netmgt.provision.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class PluginWrapper {
    private Map<String,Set<String>> m_required = new TreeMap<String,Set<String>>();
    private Map<String,Set<String>> m_optional = new TreeMap<String,Set<String>>();
    
    private final String m_className;

    public PluginWrapper(String className) throws ClassNotFoundException {
        this(Class.forName(className));
    }
    
    public PluginWrapper(Class<?> clazz) throws ClassNotFoundException {
        m_className = clazz.getName();
        BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(m_className));

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (pd.getName().equals("class")) {
                continue;
            }
            Method m = pd.getReadMethod();
            if (m.isAnnotationPresent(Require.class)) {
                Set<String> values = new TreeSet<String>();
                Require a = m.getAnnotation(Require.class);
                for (String key: a.value()) {
                    values.add(key);
                }
                m_required.put(pd.getName(), values);
            } else {
                m_optional.put(pd.getName(), new HashSet<String>());
            }
        }
    }

    public String getClassName() {
        return m_className;
    }

    public Map<String,Boolean> getRequired() {
        Map<String,Boolean> ret = new HashMap<String,Boolean>();
        for (String key : m_required.keySet()) {
            ret.put(key, true);
        }
        for (String key : m_optional.keySet()) {
            ret.put(key, false);
        }
        return ret;
    }

    public Set<String> getRequiredKeys() {
        return m_required.keySet();
    }
    public Set<String> getOptionalKeys() {
        return m_optional.keySet();
    }

    public Map<String,Set<String>> getRequiredItems() {
        return m_required;
    }
    public Map<String,Set<String>> getOptionalItems() {
        return m_optional;
    }
}
