package org.opennms.netmgt.model;


public class PollResult {
	
	private int m_id;
	private String[] m_messages;
	
	public PollResult() {
		
	}
	
	public PollResult(int id) {
		m_id = id;
	}
	
	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		m_id = id;
	}
	
	

}
