package org.opennms.netmgt.provision.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ParameterMapAdaptor extends XmlAdapter<JAXBPluginParameter[], Map<String,String>> {
    @XmlElementWrapper(name="")
    @XmlElement(name="parameter")
    JAXBPluginParameter[] parms;
    
    @Override
    public JAXBPluginParameter[] marshal(Map<String, String> v) throws Exception {
        List<JAXBPluginParameter> pluginParms = new ArrayList<JAXBPluginParameter>();
        for (Map.Entry<String,String> e : v.entrySet()) {
            pluginParms.add(new JAXBPluginParameter(e));
        }
        parms = pluginParms.toArray(new JAXBPluginParameter[pluginParms.size()]);
        return parms;
    }

    @Override
    public Map<String,String> unmarshal(JAXBPluginParameter[] v) throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        for (JAXBPluginParameter p : v) {
            m.put(p.getKey(), p.getValue());
        }
        return m;
    }

}
