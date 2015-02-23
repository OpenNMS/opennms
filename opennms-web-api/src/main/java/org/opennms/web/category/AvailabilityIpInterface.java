package org.opennms.web.category;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsIpInterface;

@XmlRootElement(name="ipinterface")
@XmlAccessorType(XmlAccessType.NONE)
public class AvailabilityIpInterface {
    @XmlAttribute(name="id")
    private final Integer m_id;

    @XmlAttribute(name="address")
    private final String m_address;

    @XmlAttribute(name="availability")
    private final double m_availability;

    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private final List<AvailabilityMonitoredService> m_services = new ArrayList<>();

    public AvailabilityIpInterface() {
        m_id = -1;
        m_address = "";
        m_availability = -1d;
    }

    public AvailabilityIpInterface(final OnmsIpInterface iface, final double availability) {
        m_id = iface.getId();
        m_address = str(iface.getIpAddress());
        m_availability = availability;
    }

    public void addService(final AvailabilityMonitoredService service) {
        m_services.add(service);
    }
}
