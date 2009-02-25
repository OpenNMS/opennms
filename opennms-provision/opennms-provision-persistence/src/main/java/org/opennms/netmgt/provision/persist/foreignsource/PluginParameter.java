package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class PluginParameter {
    @XmlAttribute(name="key")
    private String m_key = null;

    @XmlAttribute(name="value")
    private String m_value = null;

    public PluginParameter() {
    }
    
    public PluginParameter(String key, String value) {
        m_key = key;
        m_value = value;
    }

    public PluginParameter(Entry<String, String> e) {
        m_key = e.getKey();
        m_value = e.getValue();
    }

    public String getKey() {
        return m_key;
    }
    public String getValue() {
        return m_value;
    }
    public void setKey(String key) {
        m_key = key;
    }
    public void setValue(String value) {
        m_value = value;
    }

}
