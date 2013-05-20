/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
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

package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@XmlRootElement(name="user")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnmsUser implements UserDetails {
    private static final long serialVersionUID = -5750203994158854220L;

    @XmlElement(name="user-id", required=true)
    private String m_username;
    
    @XmlElement(name="full-name", required=false)
	private String m_fullName;
    
    @XmlElement(name="user-comments", required=false)
	private String m_comments;
    
    @XmlElement(name="password", required=false)
	private String m_password;

    @XmlElement(name="passwordSalt", required=false)
    private boolean m_passwordSalted = false;

    @XmlTransient
	private Collection<? extends GrantedAuthority> m_authorities;

    @XmlElement(name="duty-schedule", required=false)
    private List<String> m_dutySchedule = new ArrayList<String>();
	
	public OnmsUser() { }

	public OnmsUser(final String username) {
	    m_username = username;
    }

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
    @Override
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
	
	public boolean getPasswordSalted() {
	    return m_passwordSalted;
	}
	
	public void setPasswordSalted(final boolean passwordSalted) {
	    m_passwordSalted = passwordSalted;
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
    @Override
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

	public List<String> getDutySchedule() {
	    return m_dutySchedule;
	}

	public void setDutySchedule(final List<String> dutySchedule) {
	    m_dutySchedule = dutySchedule;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("username", m_username)
            .append("full-name", m_fullName)
            .append("comments", m_comments)
            .toString();
    }

	/**
	 * <p>getAuthorities</p>
	 *
	 * @return an array of {@link org.springframework.security.GrantedAuthority} objects.
	 */
    @Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return m_authorities;
	}
	
	/**
	 * <p>setAuthorities</p>
	 *
	 * @param authorities an array of {@link org.springframework.security.GrantedAuthority} objects.
	 */
	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		m_authorities = authorities;
	}

	/**
	 * <p>isAccountNonExpired</p>
	 *
	 * @return a boolean.
	 */
    @Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * <p>isAccountNonLocked</p>
	 *
	 * @return a boolean.
	 */
    @Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * <p>isCredentialsNonExpired</p>
	 *
	 * @return a boolean.
	 */
    @Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * <p>isEnabled</p>
	 *
	 * @return a boolean.
	 */
    @Override
	public boolean isEnabled() {
		return true;
	}

    public void addAuthority(final GrantedAuthority authority) {
    	final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
    	if (m_authorities != null) authorities.addAll(m_authorities);
    	authorities.add(authority);
    	m_authorities = authorities;
    }

}
