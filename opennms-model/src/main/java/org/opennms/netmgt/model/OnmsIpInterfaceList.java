package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "ipInterfaces")
public class OnmsIpInterfaceList extends LinkedList<OnmsIpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;
    private int m_totalCount;

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
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }
    
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    public void setTotalCount(int count) {
        m_totalCount = count;
    }

}
