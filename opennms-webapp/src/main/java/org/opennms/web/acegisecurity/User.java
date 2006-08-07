package org.opennms.web.acegisecurity;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

public class User implements UserDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String m_username;
	private String m_fullName;
	private String m_comments;
	private String m_password;
	//private Set m_contactInformation;
	//private Set m_dutySchedules;
	private GrantedAuthority[] m_authorities;
	
	public String getComments() {
		return m_comments;
	}
	
	public void setComments(String comments) {
		m_comments = comments;
	}
	
	public String getPassword() {
		return m_password;
	}
	
	public void setPassword(String password) {
		m_password = password;
	}
	
	public String getFullName() {
		return m_fullName;
	}
	
	public void setFullName(String fullName) {
		m_fullName = fullName;
	}
	
	public String getUsername() {
		return m_username;
	}
	
	public void setUsername(String username) {
		m_username = username;
	}
    
    public String toString() {
    	return "Username " + m_username + " full name " + m_fullName + " comments " + m_comments + " password " + m_password;
    }

	public GrantedAuthority[] getAuthorities() {
		return m_authorities;
	}
	
	public void setAuthorities(GrantedAuthority[] authorities) {
		m_authorities = authorities;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}
}
