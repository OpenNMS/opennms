package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionInterfaceCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="interfaces")
public class RequisitionInterfaceCollection extends LinkedList<RequisitionInterface> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RequisitionInterfaceCollection.</p>
	 */
	public RequisitionInterfaceCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionInterfaceCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionInterfaceCollection(Collection<? extends RequisitionInterface> c) {
        super(c);
    }

    /**
     * <p>getInterfaces</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="interface")
    public List<RequisitionInterface> getInterfaces() {
        return this;
    }

    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces a {@link java.util.List} object.
     */
    public void setInterfaces(List<RequisitionInterface> interfaces) {
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
}

