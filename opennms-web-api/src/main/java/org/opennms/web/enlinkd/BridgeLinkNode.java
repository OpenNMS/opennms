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

import java.util.ArrayList;
import java.util.List;

public class BridgeLinkNode implements Comparable<BridgeLinkNode>{
	
    private String m_bridgeLocalPort;
    private String m_bridgeLocalPortUrl;
    private List<BridgeLinkRemoteNode> m_bridgeLinkRemoteNodes = new ArrayList<BridgeLinkRemoteNode>();
    private String m_bridgeInfo;
    private String m_bridgeLinkCreateTime;
    private String m_bridgeLinkLastPollTime;

    public String getBridgeInfo() {
        return m_bridgeInfo;
    }
    public void setBridgeInfo(String bridgeInfo) {
        m_bridgeInfo = bridgeInfo;
    }
    public String getBridgeLocalPortUrl() {
        return m_bridgeLocalPortUrl;
    }
    public void setBridgeLocalPortUrl(String bridgeLocalPortUrl) {
        m_bridgeLocalPortUrl = bridgeLocalPortUrl;
    }
    public String getBridgeLocalPort() {
        return m_bridgeLocalPort;
    }
    public void setBridgeLocalPort(String nodeLocalPort) {
        m_bridgeLocalPort = nodeLocalPort;
    }
    public List<BridgeLinkRemoteNode> getBridgeLinkRemoteNodes() {
        return m_bridgeLinkRemoteNodes;
    }
    public void setBridgeLinkRemoteNodes(
            List<BridgeLinkRemoteNode> bridgeLinkRemoteNodes) {
        m_bridgeLinkRemoteNodes = bridgeLinkRemoteNodes;
    }
    
    public String getBridgeLinkCreateTime() {
        return m_bridgeLinkCreateTime;
    }
    
    public void setBridgeLinkCreateTime(String bridgeLinkCreateTime) {
        m_bridgeLinkCreateTime = bridgeLinkCreateTime;
    }
    
    public String getBridgeLinkLastPollTime() {
        return m_bridgeLinkLastPollTime;
    }
    
    public void setBridgeLinkLastPollTime(String bridgeLinkLastPollTime) {
        m_bridgeLinkLastPollTime = bridgeLinkLastPollTime;
    }
    
    @Override
    public int compareTo(BridgeLinkNode o) {
        return m_bridgeLocalPort.compareTo(o.m_bridgeLocalPort);
    }
    
    

}

