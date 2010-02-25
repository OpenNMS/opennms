package org.opennms.poller.remote;

public class AuthenticationBean {
	private String m_username;
	private String m_password;
	
	public void setUsername(String username) {
		m_username = username;
	}
	public String getUsername() {
		return m_username;
	}
	
	public void setPassword(String password) {
		m_password = password;
	}
	public String getPassword() {
		return m_password;
	}
}
