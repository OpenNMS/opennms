package org.opennms.netmgt.ncs.northbounder.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ServiceAlarmNotification")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceAlarmNotification {

	@XmlElement(name="ServiceAlarm")
	List<ServiceAlarm> m_serviceAlarms;
	
	public ServiceAlarmNotification() {
	}

	public ServiceAlarmNotification(List<ServiceAlarm> alarms) {
		m_serviceAlarms = alarms;
	}

	public List<ServiceAlarm> getServiceAlarms() {
		return m_serviceAlarms;
	}

	public void setServiceAlarms(List<ServiceAlarm> serviceAlarms) {
		m_serviceAlarms = serviceAlarms;
	}
	

}
