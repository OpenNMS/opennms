package org.opennms.netmgt.provision.persist;

import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class PluginParameter {
    @XmlAttribute
    private String key = null;
    @XmlAttribute
    private String value = null;

    public PluginParameter() {
    }
    
    public PluginParameter(Entry<String, String> e) {
        key = e.getKey();
        value = e.getValue();
    }

    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }

}
