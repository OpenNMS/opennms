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
package org.opennms.web.enlinkd;

public class LldpElementNode {

	private String m_lldpChassisId;
	private String m_lldpSysName;
	private String m_lldpCreateTime;
	private String m_lldpLastPollTime;
	
	
	public String getLldpChassisId() {
		return m_lldpChassisId;
	}
	public void setLldpChassisId(String lldpChassisId) {
		m_lldpChassisId = lldpChassisId;
	}
	public String getLldpSysName() {
		return m_lldpSysName;
	}
	public void setLldpSysName(String lldpSysName) {
		m_lldpSysName = lldpSysName;
	}
	public String getLldpCreateTime() {
		return m_lldpCreateTime;
	}
	public void setLldpCreateTime(String lldpCreateTime) {
		m_lldpCreateTime = lldpCreateTime;
	}
	public String getLldpLastPollTime() {
		return m_lldpLastPollTime;
	}
	public void setLldpLastPollTime(String lldpLastPollTime) {
		m_lldpLastPollTime = lldpLastPollTime;
	}
	
}
