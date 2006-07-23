package org.opennms.netmgt.model;

import java.util.Date;
import java.util.Set;

public class DemandPoll {
	
	private int m_id;
	private Date m_requestTime;
	private String m_user;
	private String m_description;
	private Set<PollResult> m_pollResults;
	
	public String getDescription() {
		return m_description;
	}
	public void setDescription(String description) {
		m_description = description;
	}
	public int getId() {
		return m_id;
	}
	public void setId(int id) {
		m_id = id;
	}
	public Set<PollResult> getPollResults() {
		return m_pollResults;
	}
	public void setPollResults(Set<PollResult> pollResults) {
		m_pollResults = pollResults;
	}
	public Date getRequestTime() {
		return m_requestTime;
	}
	public void setRequestTime(Date requestTime) {
		m_requestTime = requestTime;
	}
	public String getUser() {
		return m_user;
	}
	public void setUser(String user) {
		m_user = user;
	}

	
}
