package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>OnmsNodeList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "nodes")
public class OnmsNodeList extends LinkedList<OnmsNode> {

    private static final long serialVersionUID = 8031737923157780179L;
    private int m_totalCount;
    
    /**
     * <p>Constructor for OnmsNodeList.</p>
     */
    public OnmsNodeList() {
        super();
    }

    /**
     * <p>Constructor for OnmsNodeList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsNodeList(Collection<? extends OnmsNode> c) {
        super(c);
    }

    /**
     * <p>getNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "node")
    public List<OnmsNode> getNodes() {
        return this;
    }
    
    /**
     * <p>setNodes</p>
     *
     * @param nodes a {@link java.util.List} object.
     */
    public void setNodes(List<OnmsNode> nodes) {
        clear();
        addAll(nodes);
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
