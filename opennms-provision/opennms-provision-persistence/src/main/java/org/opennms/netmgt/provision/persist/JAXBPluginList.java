package org.opennms.netmgt.provision.persist;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="pluginConfigParameter")
public class JAXBPluginList {
    public List<JAXBPluginParameter> parameter;
    public JAXBPluginList() {
        parameter = new LinkedList<JAXBPluginParameter>();
    }

    public JAXBPluginList(Map<String,String> m) {
        parameter = new LinkedList<JAXBPluginParameter>();
        for (Map.Entry<String,String> e : m.entrySet()) {
            parameter.add(new JAXBPluginParameter(e));
        }
    }

    public void setParameter(List<JAXBPluginParameter> list) {
        parameter = list;
    }
    
    public List<JAXBPluginParameter> getParameter() {
        return parameter;
    }
}
