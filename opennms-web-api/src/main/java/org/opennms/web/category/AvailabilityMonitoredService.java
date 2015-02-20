package org.opennms.web.category;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsMonitoredService;

@XmlRootElement(name="service")
public class AvailabilityMonitoredService {
    @XmlAttribute(name="id")
    private final Integer m_id;

    @XmlAttribute(name="name")
    private final String m_name;

    @XmlAttribute(name="availability")
    private final double m_availability;

    public AvailabilityMonitoredService() {
        m_id = -1;
        m_name = "";
        m_availability = -1d;
    }

    public AvailabilityMonitoredService(final OnmsMonitoredService svc, final double availability) {
        m_id = svc.getId();
        m_name = svc.getServiceName();
        m_availability = availability;
    }
}
