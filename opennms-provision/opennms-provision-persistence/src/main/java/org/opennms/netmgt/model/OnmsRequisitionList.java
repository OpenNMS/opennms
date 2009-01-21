package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.OnmsRequisition;

@XmlRootElement(name = "requisitions")
public class OnmsRequisitionList extends LinkedList<OnmsRequisition> {

    private static final long serialVersionUID = 1;
    
    public OnmsRequisitionList() {
        super();
    }

    public OnmsRequisitionList(Collection<? extends OnmsRequisition> c) {
        super(c);
    }

    @XmlElement(name = "node")
    public List<OnmsRequisition> getRequisitions() {
        return this;
    }
    
    public void setRequisitions(List<OnmsRequisition> requisitions) {
        clear();
        addAll(requisitions);
    }

}
