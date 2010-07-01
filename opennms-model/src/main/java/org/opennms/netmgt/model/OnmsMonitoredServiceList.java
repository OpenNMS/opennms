package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsMonitoredServiceList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "services")
public class OnmsMonitoredServiceList extends LinkedList<OnmsMonitoredService> {

    private static final long serialVersionUID = 8031737923157780179L;
    
    /**
     * <p>Constructor for OnmsMonitoredServiceList.</p>
     */
    public OnmsMonitoredServiceList() {
        super();
    }

    /**
     * <p>Constructor for OnmsMonitoredServiceList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsMonitoredServiceList(Collection<? extends OnmsMonitoredService> c) {
        super(c);
    }

    /**
     * <p>getServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "service")
    public List<OnmsMonitoredService> getServices() {
        return this;
    }
    
    /**
     * <p>setServices</p>
     *
     * @param services a {@link java.util.List} object.
     */
    public void setServices(List<OnmsMonitoredService> services) {
        clear();
        addAll(services);
    }

}
