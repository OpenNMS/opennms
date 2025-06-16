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

public class LldpLinkNode implements Comparable<LldpLinkNode> {

	private String   m_lldpLocalPort;
	private String   m_lldpLocalPortUrl;
	private String   m_lldpRemChassisId;
        private String   m_lldpRemChassisIdUrl;
	private String   m_lldpRemInfo;
	private String   m_lldpRemPort;
	private String   m_lldpRemPortUrl;
	private String   m_lldpCreateTime;
	private String   m_lldpLastPollTime;

	
	public String getLldpLocalPort() {
        return m_lldpLocalPort;
    }
    public void setLldpLocalPort(String lldpLocalPort) {
        m_lldpLocalPort = lldpLocalPort;
    }
    public String getLldpLocalPortUrl() {
        return m_lldpLocalPortUrl;
    }
    public void setLldpLocalPortUrl(String lldpLocalPortUrl) {
        m_lldpLocalPortUrl = lldpLocalPortUrl;
    }
    public String getLldpRemChassisId() {
        return m_lldpRemChassisId;
    }
    public void setLldpRemChassisId(String lldpRemChassisId) {
        m_lldpRemChassisId = lldpRemChassisId;
    }
    public String getLldpRemChassisIdUrl() {
        return m_lldpRemChassisIdUrl;
    }
    public void setLldpRemChassisIdUrl(String lldpRemChassisIdUrl) {
        m_lldpRemChassisIdUrl = lldpRemChassisIdUrl;
    }
    public String getLldpRemInfo() {
        return m_lldpRemInfo;
    }
    public void setLldpRemInfo(String lldpRemInfo) {
        m_lldpRemInfo = lldpRemInfo;
    }
    public String getLldpRemPort() {
        return m_lldpRemPort;
    }
    public void setLldpRemPort(String lldpRemPort) {
        m_lldpRemPort = lldpRemPort;
    }
    public String getLldpRemPortUrl() {
        return m_lldpRemPortUrl;
    }
    public void setLldpRemPortUrl(String lldpRemPortUrl) {
        m_lldpRemPortUrl = lldpRemPortUrl;
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
    @Override
    public int compareTo(LldpLinkNode o) {
        return m_lldpLocalPort.compareTo(o.m_lldpLocalPort);
    }
	
}
