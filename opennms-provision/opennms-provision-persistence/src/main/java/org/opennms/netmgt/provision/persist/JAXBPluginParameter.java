package org.opennms.netmgt.provision.persist;

import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"key", "value"})
public class JAXBPluginParameter {
    @XmlAttribute
    private String key = null;
    @XmlAttribute
    private String value = null;

    public JAXBPluginParameter() {
    }
    
    public JAXBPluginParameter(Entry<String, String> e) {
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
