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

public class IsisLinkNode implements Comparable<IsisLinkNode>{
	
	private Integer m_isisCircIfIndex;
	private String  m_isisCircAdminState;
    
	private String  m_isisISAdjNeighSysID;
	private String  m_isisISAdjNeighSysType;
	private String  m_isisISAdjNeighSysUrl;

	private String  m_isisISAdjNeighSNPAAddress;
	private String  m_isisISAdjNeighPort;
	private String  m_isisISAdjState;
	private Integer m_isisISAdjNbrExtendedCircID;
	private String  m_isisISAdjUrl;

	private String  m_isisLinkCreateTime;
    private String  m_isisLinkLastPollTime;
	public Integer getIsisCircIfIndex() {
		return m_isisCircIfIndex;
	}
	public void setIsisCircIfIndex(Integer isisCircIfIndex) {
		m_isisCircIfIndex = isisCircIfIndex;
	}
	public String getIsisCircAdminState() {
		return m_isisCircAdminState;
	}
	public void setIsisCircAdminState(String isisCircAdminState) {
		m_isisCircAdminState = isisCircAdminState;
	}
	public String getIsisISAdjNeighSysID() {
		return m_isisISAdjNeighSysID;
	}
	public void setIsisISAdjNeighSysID(String isisISAdjNeighSysID) {
		m_isisISAdjNeighSysID = isisISAdjNeighSysID;
	}
	public String getIsisISAdjNeighSysType() {
		return m_isisISAdjNeighSysType;
	}
	public void setIsisISAdjNeighSysType(String isisISAdjNeighSysType) {
		m_isisISAdjNeighSysType = isisISAdjNeighSysType;
	}
	public String getIsisISAdjNeighSysUrl() {
		return m_isisISAdjNeighSysUrl;
	}
	public void setIsisISAdjNeighSysUrl(String isisISAdjNeighSysUrl) {
		m_isisISAdjNeighSysUrl = isisISAdjNeighSysUrl;
	}
	public String getIsisISAdjNeighSNPAAddress() {
		return m_isisISAdjNeighSNPAAddress;
	}
	public void setIsisISAdjNeighSNPAAddress(String isisISAdjNeighSNPAAddress) {
		m_isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
	}
	public String getIsisISAdjNeighPort() {
		return m_isisISAdjNeighPort;
	}
	public void setIsisISAdjNeighPort(String isisISAdjNeighPort) {
		m_isisISAdjNeighPort = isisISAdjNeighPort;
	}
	public String getIsisISAdjState() {
		return m_isisISAdjState;
	}
	public void setIsisISAdjState(String isisISAdjState) {
		m_isisISAdjState = isisISAdjState;
	}
	public Integer getIsisISAdjNbrExtendedCircID() {
		return m_isisISAdjNbrExtendedCircID;
	}
	public void setIsisISAdjNbrExtendedCircID(Integer isisISAdjNbrExtendedCircID) {
		m_isisISAdjNbrExtendedCircID = isisISAdjNbrExtendedCircID;
	}
	public String getIsisISAdjUrl() {
		return m_isisISAdjUrl;
	}
	public void setIsisISAdjUrl(String isisISAdjUrl) {
		m_isisISAdjUrl = isisISAdjUrl;
	}
	public String getIsisLinkCreateTime() {
		return m_isisLinkCreateTime;
	}
	public void setIsisLinkCreateTime(String isisLinkCreateTime) {
		m_isisLinkCreateTime = isisLinkCreateTime;
	}
	public String getIsisLinkLastPollTime() {
		return m_isisLinkLastPollTime;
	}
	public void setIsisLinkLastPollTime(String isisLinkLastPollTime) {
		m_isisLinkLastPollTime = isisLinkLastPollTime;
	}
    @Override
    public int compareTo(IsisLinkNode o) {
        return m_isisCircIfIndex-o.m_isisCircIfIndex;
    }


}
