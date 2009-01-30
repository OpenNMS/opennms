package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.requisition.OnmsRequisition;

@XmlRootElement(name="requisitions")
public class OnmsRequisitionCollection extends LinkedList<OnmsRequisition> {

	private static final long serialVersionUID = 1L;

	public OnmsRequisitionCollection() {
        super();
    }

    public OnmsRequisitionCollection(Collection<? extends OnmsRequisition> c) {
        super(c);
    }

    @XmlElement(name="onmsRequisition")
    public List<OnmsRequisition> getRequisitions() {
        return this;
    }

    public void setRequisitions(List<OnmsRequisition> requisitions) {
        clear();
        addAll(requisitions);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

