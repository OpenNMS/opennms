package org.opennms.netmgt.provision.persist.foreignsource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement
/**
 * <p>ParameterList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="pluginConfigParameter")
public class ParameterList {
    public List<PluginParameter> parameter;
    /**
     * <p>Constructor for ParameterList.</p>
     */
    public ParameterList() {
        parameter = new LinkedList<PluginParameter>();
    }

    /**
     * <p>Constructor for ParameterList.</p>
     *
     * @param m a {@link java.util.Map} object.
     */
    public ParameterList(Map<String,String> m) {
        parameter = new LinkedList<PluginParameter>();
        for (Map.Entry<String,String> e : m.entrySet()) {
            parameter.add(new PluginParameter(e));
        }
    }

    /**
     * <p>Setter for the field <code>parameter</code>.</p>
     *
     * @param list a {@link java.util.List} object.
     */
    public void setParameter(List<PluginParameter> list) {
        parameter = list;
    }
    
    /**
     * <p>Getter for the field <code>parameter</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PluginParameter> getParameter() {
        return parameter;
    }
}
