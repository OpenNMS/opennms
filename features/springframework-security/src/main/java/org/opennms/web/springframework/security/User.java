/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.springframework.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

/**
 * <p>User class.</p>
 */
public class User implements UserDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8649420222794568157L;
	private String m_username;
	private String m_fullName;
	private String m_comments;
	private String m_password;
	//private Set m_contactInformation;
	//private Set m_dutySchedules;
	private GrantedAuthority[] m_authorities;
	
	/**
	 * <p>getComments</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getComments() {
		return m_comments;
	}
	
	/**
	 * <p>setComments</p>
	 *
	 * @param comments a {@link java.lang.String} object.
	 */
	public void setComments(String comments) {
		m_comments = comments;
	}
	
	/**
	 * <p>getPassword</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPassword() {
		return m_password;
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
	 * <p>getFullName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFullName() {
		return m_fullName;
	}
	
	/**
	 * <p>setFullName</p>
	 *
	 * @param fullName a {@link java.lang.String} object.
	 */
	public void setFullName(String fullName) {
		m_fullName = fullName;
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
	 * <p>setUsername</p>
	 *
	 * @param username a {@link java.lang.String} object.
	 */
	public void setUsername(String username) {
		m_username = username;
	}
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
    	return "Username " + m_username + " full name " + m_fullName + " comments " + m_comments + " password " + m_password;
    }

	/**
	 * <p>getAuthorities</p>
	 *
	 * @return an array of {@link org.springframework.security.GrantedAuthority} objects.
	 */
	public GrantedAuthority[] getAuthorities() {
		return m_authorities;
	}
	
	/**
	 * <p>setAuthorities</p>
	 *
	 * @param authorities an array of {@link org.springframework.security.GrantedAuthority} objects.
	 */
	public void setAuthorities(GrantedAuthority[] authorities) {
		m_authorities = authorities;
	}

	/**
	 * <p>isAccountNonExpired</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * <p>isAccountNonLocked</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * <p>isCredentialsNonExpired</p>
	 *
	 * @return a boolean.
	 */
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * <p>isEnabled</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEnabled() {
		return true;
	}
}
