package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>OnmsIpInterfaceList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "ipInterfaces")
public class OnmsIpInterfaceList extends LinkedList<OnmsIpInterface> {

    private static final long serialVersionUID = 1123252152117491694L;
    private int m_totalCount;

    /**
     * <p>Constructor for OnmsIpInterfaceList.</p>
     */
    public OnmsIpInterfaceList() {
        super();
    }

    /**
     * <p>Constructor for OnmsIpInterfaceList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsIpInterfaceList(Collection<? extends OnmsIpInterface> c) {
        super(c);
    }

    /**
     * <p>getInterfaces</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "ipInterface")
    public List<OnmsIpInterface> getInterfaces() {
        return this;
    }
    
    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces a {@link java.util.List} object.
     */
    public void setInterfaces(List<OnmsIpInterface> interfaces) {
        clear();
        addAll(interfaces);
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

}
