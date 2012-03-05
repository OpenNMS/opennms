package org.opennms.netmgt.ncs.northbounder.transfer;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ServiceAlarm")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceAlarm {
	
	@XmlElement(name="Id")
	private String m_id;

	@XmlElement(name="Name")
	private String m_name;

	@XmlElement(name="Status")
	private String m_status;

	public ServiceAlarm() {}

	public ServiceAlarm(String id, String name, String status) {
		m_id = id;
		m_name = name;
		m_status = status;
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getStatus() {
		return m_status;
	}

	public void setStatus(String status) {
		m_status = status;
	}

}
