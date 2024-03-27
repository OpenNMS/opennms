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

public class OspfLinkNode implements Comparable<OspfLinkNode>{

    private String m_ospfLocalPort;
    private String m_ospfLocalPortUrl;

    private String m_ospfRemRouterId;
    private String m_ospfRemRouterUrl;

    private String m_ospfRemPort;
    private String m_ospfRemPortUrl;
    
    private String m_ospfLinkInfo;

    private String m_ospfLinkCreateTime;
    private String m_ospfLinkLastPollTime;

    public String getOspfLocalPort() {
        return m_ospfLocalPort;
    }

    public void setOspfLocalPort(String ospfLocalPort) {
        m_ospfLocalPort = ospfLocalPort;
    }

    public String getOspfLocalPortUrl() {
        return m_ospfLocalPortUrl;
    }

    public void setOspfLocalPortUrl(String ospfLocalPortUrl) {
        m_ospfLocalPortUrl = ospfLocalPortUrl;
    }

    public String getOspfRemRouterId() {
        return m_ospfRemRouterId;
    }

    public void setOspfRemRouterId(String ospfRemRouterId) {
        m_ospfRemRouterId = ospfRemRouterId;
    }

    public String getOspfRemRouterUrl() {
        return m_ospfRemRouterUrl;
    }

    public void setOspfRemRouterUrl(String ospfRemRouterUrl) {
        m_ospfRemRouterUrl = ospfRemRouterUrl;
    }

    public String getOspfRemPort() {
        return m_ospfRemPort;
    }

    public void setOspfRemPort(String ospfRemPort) {
        m_ospfRemPort = ospfRemPort;
    }

    public String getOspfRemPortUrl() {
        return m_ospfRemPortUrl;
    }

    public void setOspfRemPortUrl(String ospfRemPortUrl) {
        m_ospfRemPortUrl = ospfRemPortUrl;
    }

    public String getOspfLinkInfo() {
        return m_ospfLinkInfo;
    }

    public void setOspfLinkInfo(String ospfLinkInfo) {
        m_ospfLinkInfo = ospfLinkInfo;
    }

    public String getOspfLinkCreateTime() {
        return m_ospfLinkCreateTime;
    }

    public void setOspfLinkCreateTime(String ospfLinkCreateTime) {
        m_ospfLinkCreateTime = ospfLinkCreateTime;
    }

    public String getOspfLinkLastPollTime() {
        return m_ospfLinkLastPollTime;
    }

    public void setOspfLinkLastPollTime(String ospfLinkLastPollTime) {
        m_ospfLinkLastPollTime = ospfLinkLastPollTime;
    }

    @Override
    public int compareTo(OspfLinkNode o) {
        return getOspfLocalPort().compareTo(o.getOspfLocalPort());
    }

}
