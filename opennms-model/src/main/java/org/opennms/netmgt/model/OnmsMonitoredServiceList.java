package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "services")
public class OnmsMonitoredServiceList extends LinkedList<OnmsMonitoredService> {

    private static final long serialVersionUID = 8031737923157780179L;
    
    public OnmsMonitoredServiceList() {
        super();
    }

    public OnmsMonitoredServiceList(Collection<? extends OnmsMonitoredService> c) {
        super(c);
    }

    @XmlElement(name = "service")
    public List<OnmsMonitoredService> getServices() {
        return this;
    }
    
    public void setServices(List<OnmsMonitoredService> services) {
        clear();
        addAll(services);
    }

}
