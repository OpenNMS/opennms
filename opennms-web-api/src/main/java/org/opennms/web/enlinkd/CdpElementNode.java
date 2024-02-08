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

public class CdpElementNode {

    private String m_cdpGlobalRun;
    private String m_cdpGlobalDeviceId;
    private String m_cdpGlobalDeviceIdFormat;
    private String m_cdpCreateTime;
    private String m_cdpLastPollTime;

    public String getCdpCreateTime() {
        return m_cdpCreateTime;
    }

    public void setCdpCreateTime(String cdpCreateTime) {
        m_cdpCreateTime = cdpCreateTime;
    }

    public String getCdpLastPollTime() {
        return m_cdpLastPollTime;
    }

    public void setCdpLastPollTime(String cdpLastPollTime) {
        m_cdpLastPollTime = cdpLastPollTime;
    }

    public String getCdpGlobalRun() {
        return m_cdpGlobalRun;
    }

    public void setCdpGlobalRun(String cdpGlobalRun) {
        m_cdpGlobalRun = cdpGlobalRun;
    }

    public String getCdpGlobalDeviceId() {
        return m_cdpGlobalDeviceId;
    }

    public void setCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        m_cdpGlobalDeviceId = cdpGlobalDeviceId;
    }

    public String getCdpGlobalDeviceIdFormat() {
        return m_cdpGlobalDeviceIdFormat;
    }

    public void setCdpGlobalDeviceIdFormat(String cdpGlobalDeviceIdFormat) {
        m_cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
    }

}
