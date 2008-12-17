package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

public class JAXBPluginParameterWrapper {
    @XmlElement(name="parameter")
    private final List<JAXBPluginParameter> parameters = new ArrayList<JAXBPluginParameter>();
    
    JAXBPluginParameterWrapper() {
    }

    JAXBPluginParameterWrapper(Map<String,String> m) {
        for (Map.Entry<String,String> e : m.entrySet()) {
            parameters.add(new JAXBPluginParameter(e));
        }
    }
    
    public Map<String,String> getMap() {
        Map<String,String> m = new HashMap<String,String>();
        for (JAXBPluginParameter p : parameters) {
            m.put(p.getKey(), p.getValue());
        }
        return m;
    }
}
