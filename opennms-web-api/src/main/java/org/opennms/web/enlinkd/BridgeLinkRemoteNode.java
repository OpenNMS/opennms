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

public class BridgeLinkRemoteNode {
	
    private String m_bridgeRemote;
    private String m_bridgeRemoteUrl;
    private String m_bridgeRemotePort;
    private String m_bridgeRemotePortUrl;

    public String getBridgeRemote() {
        return m_bridgeRemote;
    }

    public void setBridgeRemote(String bridgeRemote) {
        m_bridgeRemote = bridgeRemote;
    }

    public String getBridgeRemoteUrl() {
        return m_bridgeRemoteUrl;
    }

    public void setBridgeRemoteUrl(String bridgeRemoteUrl) {
        m_bridgeRemoteUrl = bridgeRemoteUrl;
    }

    public String getBridgeRemotePort() {
        return m_bridgeRemotePort;
    }

    public void setBridgeRemotePort(String bridgeRemotePort) {
        m_bridgeRemotePort = bridgeRemotePort;
    }

    public String getBridgeRemotePortUrl() {
        return m_bridgeRemotePortUrl;
    }

    public void setBridgeRemotePortUrl(String bridgeRemotePortUrl) {
        m_bridgeRemotePortUrl = bridgeRemotePortUrl;
    }

}
