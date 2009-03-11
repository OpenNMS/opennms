package org.opennms.web.svclayer.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opennms.netmgt.provision.annotations.Allow;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class PluginWrapper {
    private Map<String,Set<String>> m_required = new TreeMap<String,Set<String>>();
    private Map<String,Set<String>> m_optional = new TreeMap<String,Set<String>>();
    
    private final String m_className;

    public PluginWrapper(String className) throws ClassNotFoundException {
        m_className = className;
        BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(m_className));

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (pd.getName().equals("class")) {
                continue;
            }
            Method m = pd.getReadMethod();
            if (m.isAnnotationPresent(Allow.class)) {
                Set<String> values = new TreeSet<String>();
                Allow a = m.getAnnotation(Allow.class);
                for (String key: a.value()) {
                    values.add(key);
                }
                m_required.put(pd.getName(), values);
            } else {
                m_optional.put(pd.getName(), null);
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

    public Map<String,Set<String>> getRequiredValues() {
        return m_required;
    }
    public Map<String,Set<String>> getOptionalValues() {
        return m_optional;
    }
}
