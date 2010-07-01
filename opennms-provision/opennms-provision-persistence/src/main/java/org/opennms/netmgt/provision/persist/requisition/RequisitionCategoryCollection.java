package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionCategoryCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="categories")
public class RequisitionCategoryCollection extends LinkedList<RequisitionCategory> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RequisitionCategoryCollection.</p>
	 */
	public RequisitionCategoryCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionCategoryCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionCategoryCollection(Collection<? extends RequisitionCategory> c) {
        super(c);
    }

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="category")
    public List<RequisitionCategory> getCategories() {
        return this;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<RequisitionCategory> categories) {
        clear();
        addAll(categories);
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

