package org.opennms.poller.remote;

/**
 * <p>AuthenticationBean class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AuthenticationBean {
	private String m_username;
	private String m_password;
	
	/**
	 * <p>setUsername</p>
	 *
	 * @param username a {@link java.lang.String} object.
	 */
	public void setUsername(String username) {
		m_username = username;
	}
	/**
	 * <p>getUsername</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUsername() {
		return m_username;
	}
	
	/**
	 * <p>setPassword</p>
	 *
	 * @param password a {@link java.lang.String} object.
	 */
	public void setPassword(String password) {
		m_password = password;
	}
	/**
	 * <p>getPassword</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPassword() {
		return m_password;
	}
}
