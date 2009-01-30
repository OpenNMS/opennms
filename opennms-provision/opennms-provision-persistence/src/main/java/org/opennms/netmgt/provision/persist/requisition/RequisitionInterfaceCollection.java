package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="interfaces")
public class RequisitionInterfaceCollection extends LinkedList<RequisitionInterface> {

	private static final long serialVersionUID = 1L;

	public RequisitionInterfaceCollection() {
        super();
    }

    public RequisitionInterfaceCollection(Collection<? extends RequisitionInterface> c) {
        super(c);
    }

    @XmlElement(name="interface")
    public List<RequisitionInterface> getInterfaces() {
        return this;
    }

    public void setInterfaces(List<RequisitionInterface> interfaces) {
        clear();
        addAll(interfaces);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

