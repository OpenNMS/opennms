/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.util.Date;
import java.util.Set;

/**
 * <p>DemandPoll class.</p>
 */
public class DemandPoll {
	
	private Integer m_id;
	private Date m_requestTime;
	private String m_userName;
	private String m_description;
	private Set<PollResult> m_pollResults;
	
	/**
	 * <p>Constructor for DemandPoll.</p>
	 */
	public DemandPoll() {
		
	}
	
	/**
	 * <p>getDescription</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDescription() {
		return m_description;
	}
	/**
	 * <p>setDescription</p>
	 *
	 * @param description a {@link java.lang.String} object.
	 */
	public void setDescription(String description) {
		m_description = description;
	}
	/**
	 * <p>getId</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getId() {
		return m_id;
	}
	/**
	 * <p>setId</p>
	 *
	 * @param id a int.
	 */
	public void setId(int id) {
		m_id = id;
	}
	/**
	 * <p>setId</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 */
	public void setId(Integer id) {
		m_id = id;
	}
	/**
	 * <p>getPollResults</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<PollResult> getPollResults() {
		return m_pollResults;
	}
	/**
	 * <p>setPollResults</p>
	 *
	 * @param pollResults a {@link java.util.Set} object.
	 */
	public void setPollResults(Set<PollResult> pollResults) {
		m_pollResults = pollResults;
	}
	/**
	 * <p>getRequestTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	public Date getRequestTime() {
		return m_requestTime;
	}
	/**
	 * <p>setRequestTime</p>
	 *
	 * @param requestTime a {@link java.util.Date} object.
	 */
	public void setRequestTime(Date requestTime) {
		m_requestTime = requestTime;
	}
	/**
	 * <p>getUserName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUserName() {
		return m_userName;
	}
	/**
	 * <p>setUserName</p>
	 *
	 * @param user a {@link java.lang.String} object.
	 */
	public void setUserName(String user) {
		m_userName = user;
	}
	
}
