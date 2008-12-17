package org.opennms.netmgt.provision.persist;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ParameterMapAdaptor extends XmlAdapter<JAXBPluginParameterWrapper, Map<String,String>> {
    @Override
    public JAXBPluginParameterWrapper marshal(Map<String, String> v) throws Exception {
        return new JAXBPluginParameterWrapper(v);
    }

    @Override
    public Map<String,String> unmarshal(JAXBPluginParameterWrapper w) throws Exception {
        return w.getMap();
    }

}
