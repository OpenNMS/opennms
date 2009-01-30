package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="nodes")
public class RequisitionNodeCollection extends LinkedList<RequisitionNode> {

	private static final long serialVersionUID = 1L;

	public RequisitionNodeCollection() {
        super();
    }

    public RequisitionNodeCollection(Collection<? extends RequisitionNode> c) {
        super(c);
    }

    @XmlElement(name="node")
    public List<RequisitionNode> getNodes() {
        return this;
    }

    public void setNodes(List<RequisitionNode> requisitions) {
        clear();
        addAll(requisitions);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

