package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="requisitions")
public class RequisitionCollection extends LinkedList<Requisition> {

	private static final long serialVersionUID = 1L;

	public RequisitionCollection() {
        super();
    }

    public RequisitionCollection(Collection<? extends Requisition> c) {
        super(c);
    }

    @XmlElement(name="model-import")
    public List<Requisition> getRequisitions() {
        return this;
    }

    public void setRequisitions(List<Requisition> requisitions) {
        clear();
        addAll(requisitions);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

