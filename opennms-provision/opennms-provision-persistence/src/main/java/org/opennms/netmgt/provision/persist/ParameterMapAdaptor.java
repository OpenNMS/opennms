package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ParameterMapAdaptor extends XmlAdapter<JAXBPluginList, Map<String,String>> {
    @Override
    public JAXBPluginList marshal(Map<String, String> v) throws Exception {
        return new JAXBPluginList(v);
    }

    @Override
    public Map<String,String> unmarshal(JAXBPluginList w) throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        for (JAXBPluginParameter p : w.getParameter()) {
            m.put(p.getKey(), p.getValue());
        }
        return m;
    }

}
