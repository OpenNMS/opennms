/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.springframework.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

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
