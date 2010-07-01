package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionNodeCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="nodes")
public class RequisitionNodeCollection extends LinkedList<RequisitionNode> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RequisitionNodeCollection.</p>
	 */
	public RequisitionNodeCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionNodeCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionNodeCollection(Collection<? extends RequisitionNode> c) {
        super(c);
    }

    /**
     * <p>getNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="node")
    public List<RequisitionNode> getNodes() {
        return this;
    }

    /**
     * <p>setNodes</p>
     *
     * @param requisitions a {@link java.util.List} object.
     */
    public void setNodes(List<RequisitionNode> requisitions) {
        clear();
        addAll(requisitions);
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
}

