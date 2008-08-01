package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "snmpInterfaces")
public class OnmsSnmpInterfaceList extends LinkedList<OnmsSnmpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;

    public OnmsSnmpInterfaceList() {
        super();
    }

    public OnmsSnmpInterfaceList(Collection<? extends OnmsSnmpInterface> c) {
        super(c);
    }

    @XmlElement(name = "snmpInterface")
    public List<OnmsSnmpInterface> getInterfaces() {
        return this;
    }
    
    public void setInterfaces(List<OnmsSnmpInterface> interfaces) {
        clear();
        addAll(interfaces);
    }

}
