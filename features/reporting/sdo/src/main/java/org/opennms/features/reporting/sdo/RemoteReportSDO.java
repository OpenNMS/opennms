package org.opennms.features.reporting.sdo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "remoteReportSDO")
public class RemoteReportSDO {

	private String m_id;
	private String m_engine;
	private String m_template;
	private String m_description;
	private String m_displayName;
	private String m_reportService;
	private boolean m_online;

	@XmlElement(name = "description")
	public String getDescription() {
		return m_description;
	}

	@XmlElement(name = "display-name")
	public String getDisplayName() {
		return m_displayName;
	}

	@XmlElement(name = "engine")
	public String getEngine() {
		return m_engine;
	}

	@XmlElement(name = "id")
	public String getId() {
		return m_id;
	}

	@XmlElement(name = "report-service")
	public String getReportService() {
		return m_reportService;
	}

	@XmlElement(name = "template")
	public String getTemplate() {
		return m_template;
	}

	@XmlElement(name = "online")
	public boolean getOnline() {
		return m_online;
	}

	public void setDescription(String description) {
		m_description = description;
	}

	public void setDisplayName(String displayName) {
		m_displayName = displayName;
	}

	public void setEngine(String engine) {
		m_engine = engine;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setOnline(boolean online) {
		m_online = online;
	}

	public void setReportService(String reportService) {
		m_reportService = reportService;
	}

	public void setTemplate(String template) {
		m_template = template;
	}
}
