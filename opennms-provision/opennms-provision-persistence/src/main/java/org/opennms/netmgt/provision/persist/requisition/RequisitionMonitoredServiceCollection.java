package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionMonitoredServiceCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="services")
public class RequisitionMonitoredServiceCollection extends LinkedList<RequisitionMonitoredService> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RequisitionMonitoredServiceCollection.</p>
	 */
	public RequisitionMonitoredServiceCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionMonitoredServiceCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionMonitoredServiceCollection(Collection<? extends RequisitionMonitoredService> c) {
        super(c);
    }

    /**
     * <p>getMonitoredServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="monitored-service")
    public List<RequisitionMonitoredService> getMonitoredServices() {
        return this;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param services a {@link java.util.List} object.
     */
    public void setMonitoredServices(List<RequisitionMonitoredService> services) {
        clear();
        addAll(services);
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

