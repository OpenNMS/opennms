package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="requisitions")
public class RequisitionCollection extends LinkedList<Requisition> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RequisitionCollection.</p>
	 */
	public RequisitionCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionCollection(Collection<? extends Requisition> c) {
        super(c);
    }

    /**
     * <p>getRequisitions</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="model-import")
    public List<Requisition> getRequisitions() {
        return this;
    }

    /**
     * <p>setRequisitions</p>
     *
     * @param requisitions a {@link java.util.List} object.
     */
    public void setRequisitions(List<Requisition> requisitions) {
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

