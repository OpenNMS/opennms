package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity()
@Table(name="scanReportLogs")
public class ScanReportLog implements Serializable {
	private static final long serialVersionUID = 1L;

	private String m_id;
	private String m_logText;

	public ScanReportLog() {
	}

	public ScanReportLog(final String id, final String logText) {
		m_id = id;
		m_logText = logText;
	}

	@Id
	@Column(name="scanReportId")
	public String getId() {
		return m_id;
	}

	public void setId(final String id) {
		m_id = id;
	}

	@Column(name="logText")
	public String getLogText() {
		return m_logText;
	}

	public void setLogText(final String logText) {
		m_logText = logText;
	}
}
