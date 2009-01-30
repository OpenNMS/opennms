package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="categories")
public class RequisitionCategoryCollection extends LinkedList<RequisitionCategory> {

	private static final long serialVersionUID = 1L;

	public RequisitionCategoryCollection() {
        super();
    }

    public RequisitionCategoryCollection(Collection<? extends RequisitionCategory> c) {
        super(c);
    }

    @XmlElement(name="category")
    public List<RequisitionCategory> getCategories() {
        return this;
    }

    public void setCategories(List<RequisitionCategory> categories) {
        clear();
        addAll(categories);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

