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


public class CdpLinkNode implements Comparable<CdpLinkNode>{

    
    private String  m_cdpLocalPort;
    private String  m_cdpLocalPortUrl;

    private String m_cdpCacheDevice;
    private String m_cdpCacheDeviceUrl;

    private String m_cdpCacheDevicePort;
    private String m_cdpCacheDevicePortUrl;

    private String m_cdpCachePlatform;
    
    private String m_cdpCreateTime;
    private String m_cdpLastPollTime;
    

    public String getCdpLocalPort() {
        return m_cdpLocalPort;
    }

    public void setCdpLocalPort(String cdpLocalPort) {
        m_cdpLocalPort = cdpLocalPort;
    }

    public String getCdpLocalPortUrl() {
        return m_cdpLocalPortUrl;
    }

    public void setCdpLocalPortUrl(String cdplocalPortUrl) {
        m_cdpLocalPortUrl = cdplocalPortUrl;
    }


    public String getCdpCacheDevice() {
        return m_cdpCacheDevice;
    }

    public void setCdpCacheDevice(String cdpCacheDevice) {
        m_cdpCacheDevice = cdpCacheDevice;
    }

    public String getCdpCacheDeviceUrl() {
        return m_cdpCacheDeviceUrl;
    }

    public void setCdpCacheDeviceUrl(String cdpCacheDeviceUrl) {
        m_cdpCacheDeviceUrl = cdpCacheDeviceUrl;
    }

    public String getCdpCacheDevicePort() {
        return m_cdpCacheDevicePort;
    }

    public void setCdpCacheDevicePort(String cdpCacheDevicePort) {
        m_cdpCacheDevicePort = cdpCacheDevicePort;
    }

    public String getCdpCacheDevicePortUrl() {
        return m_cdpCacheDevicePortUrl;
    }

    public void setCdpCacheDevicePortUrl(String cdpCacheDevicePortUrl) {
        m_cdpCacheDevicePortUrl = cdpCacheDevicePortUrl;
    }

    public String getCdpCachePlatform() {
        return m_cdpCachePlatform;
    }

    public void setCdpCachePlatform(String cdpCachePlatform) {
        m_cdpCachePlatform = cdpCachePlatform;
    }

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

    @Override
    public int compareTo(CdpLinkNode o) {
        return m_cdpLocalPort.compareTo(o.m_cdpLocalPort);
    }

}
