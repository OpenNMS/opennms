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

public class OspfElementNode {

	private String m_ospfRouterId;
	private Integer m_ospfVersionNumber;
	private String m_ospfAdminStat;
	private String m_ospfCreateTime;
	private String m_ospfLastPollTime;

	public String getOspfRouterId() {
		return m_ospfRouterId;
	}
	public void setOspfRouterId(String ospfRouterId) {
		m_ospfRouterId = ospfRouterId;
	}
	public Integer getOspfVersionNumber() {
		return m_ospfVersionNumber;
	}
	public void setOspfVersionNumber(Integer ospfVersionNumber) {
		m_ospfVersionNumber = ospfVersionNumber;
	}
	public String getOspfAdminStat() {
		return m_ospfAdminStat;
	}
	public void setOspfAdminStat(String ospfAdminStat) {
		m_ospfAdminStat = ospfAdminStat;
	}
	public String getOspfCreateTime() {
		return m_ospfCreateTime;
	}
	public void setOspfCreateTime(String ospfCreateTime) {
		m_ospfCreateTime = ospfCreateTime;
	}
	public String getOspfLastPollTime() {
		return m_ospfLastPollTime;
	}
	public void setOspfLastPollTime(String ospfLastPollTime) {
		m_ospfLastPollTime = ospfLastPollTime;
	}

}
