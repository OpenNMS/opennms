package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ipInterfaces")
public class OnmsIpInterfaceList extends LinkedList<OnmsIpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;

    public OnmsIpInterfaceList() {
        super();
    }

    public OnmsIpInterfaceList(Collection<? extends OnmsIpInterface> c) {
        super(c);
    }

    @XmlElement(name = "ipInterface")
    public List<OnmsIpInterface> getInterfaces() {
        return this;
    }
    
    public void setInterfaces(List<OnmsIpInterface> interfaces) {
        clear();
        addAll(interfaces);
    }

}
