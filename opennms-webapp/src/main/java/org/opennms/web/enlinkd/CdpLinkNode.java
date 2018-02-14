/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.enlinkd;


public class CdpLinkNode implements Comparable<CdpLinkNode>{

    
    private String  m_cdpLocalPort;
    private String  m_cdpLocalPortUrl;

    private String m_cdpCacheAddressType;
    private String m_cdpCacheAddress;
    private String m_cdpCacheVersion;
    private String m_cdpCacheDeviceId;
    private String m_cdpCacheDeviceUrl;
    private String m_cdpCacheDevicePort;
    private String m_cdpCacheDevicePortUrl;
    private String m_cdpCacheDevicePlatform;
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

    public String getCdpCacheAddressType() {
        return m_cdpCacheAddressType;
    }

    public void setCdpCacheAddressType(String cdpCacheAddressType) {
        m_cdpCacheAddressType = cdpCacheAddressType;
    }

    public String getCdpCacheAddress() {
        return m_cdpCacheAddress;
    }

    public void setCdpCacheAddress(String cdpCacheAddress) {
        m_cdpCacheAddress = cdpCacheAddress;
    }

    public String getCdpCacheVersion() {
        return m_cdpCacheVersion;
    }

    public void setCdpCacheVersion(String cdpCacheVersion) {
        m_cdpCacheVersion = cdpCacheVersion;
    }

    public String getCdpCacheDeviceId() {
        return m_cdpCacheDeviceId;
    }

    public void setCdpCacheDeviceId(String cdpCacheDeviceId) {
        m_cdpCacheDeviceId = cdpCacheDeviceId;
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

    public String getCdpCacheDevicePlatform() {
        return m_cdpCacheDevicePlatform;
    }

    public void setCdpCacheDevicePlatform(String cdpCacheDevicePlatform) {
        m_cdpCacheDevicePlatform = cdpCacheDevicePlatform;
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
