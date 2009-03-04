package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "snmpInterfaces")
public class OnmsSnmpInterfaceList extends LinkedList<OnmsSnmpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;
    private int m_totalCount;

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
    
    public void setInterfaces(List<OnmsSnmpInterface> interfaces) {
        clear();
        addAll(interfaces);
    }

}
