/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.svclayer.catstatus.model;

/**
 * <p>StatusService class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusService {

	private String m_name;
	private Boolean m_outagestatus;
	private long m_outagetime;
	
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * <p>setName</p>
	 *
	 * @param m_name a {@link java.lang.String} object.
	 */
	public void setName(String m_name) {
		this.m_name = m_name;
	}
	/**
	 * <p>getOutageStatus</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getOutageStatus() {
		return m_outagestatus;
	}
	/**
	 * <p>setOutageStatus</p>
	 *
	 * @param m_outagestatus a {@link java.lang.Boolean} object.
	 */
	public void setOutageStatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}
	/**
	 * <p>getOutageTime</p>
	 *
	 * @return a long.
	 */
	public long getOutageTime() {
		return m_outagetime;
	}
	/**
	 * <p>setOutageTime</p>
	 *
	 * @param m_outagetime a long.
	 */
	public void setOutageTime(long m_outagetime) {
		this.m_outagetime = m_outagetime;
	}
	
}
