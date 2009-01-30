package org.opennms.netmgt.provision.persist.foreignsource;

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
public class ParameterList {
    public List<PluginParameter> parameter;
    public ParameterList() {
        parameter = new LinkedList<PluginParameter>();
    }

    public ParameterList(Map<String,String> m) {
        parameter = new LinkedList<PluginParameter>();
        for (Map.Entry<String,String> e : m.entrySet()) {
            parameter.add(new PluginParameter(e));
        }
    }

    public void setParameter(List<PluginParameter> list) {
        parameter = list;
    }
    
    public List<PluginParameter> getParameter() {
        return parameter;
    }
}
