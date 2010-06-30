package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>OnmsSnmpInterfaceList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "snmpInterfaces")
public class OnmsSnmpInterfaceList extends LinkedList<OnmsSnmpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;
    private int m_totalCount;

    /**
     * <p>Constructor for OnmsSnmpInterfaceList.</p>
     */
    public OnmsSnmpInterfaceList() {
        super();
    }

    /**
     * <p>Constructor for OnmsSnmpInterfaceList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsSnmpInterfaceList(Collection<? extends OnmsSnmpInterface> c) {
        super(c);
    }

    /**
     * <p>getInterfaces</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "snmpInterface")
    public List<OnmsSnmpInterface> getInterfaces() {
        return this;
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }
    
    /**
     * <p>getTotalCount</p>
     *
     * @return a int.
     */
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    /**
     * <p>setTotalCount</p>
     *
     * @param count a int.
     */
    public void setTotalCount(int count) {
        m_totalCount = count;
    }
    
    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces a {@link java.util.List} object.
     */
    public void setInterfaces(List<OnmsSnmpInterface> interfaces) {
        clear();
        addAll(interfaces);
    }

}
