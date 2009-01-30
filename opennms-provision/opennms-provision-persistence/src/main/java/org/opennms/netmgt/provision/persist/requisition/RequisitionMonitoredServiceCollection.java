package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="services")
public class RequisitionMonitoredServiceCollection extends LinkedList<RequisitionMonitoredService> {

	private static final long serialVersionUID = 1L;

	public RequisitionMonitoredServiceCollection() {
        super();
    }

    public RequisitionMonitoredServiceCollection(Collection<? extends RequisitionMonitoredService> c) {
        super(c);
    }

    @XmlElement(name="monitored-service")
    public List<RequisitionMonitoredService> getMonitoredServices() {
        return this;
    }

    public void setMonitoredServices(List<RequisitionMonitoredService> services) {
        clear();
        addAll(services);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

