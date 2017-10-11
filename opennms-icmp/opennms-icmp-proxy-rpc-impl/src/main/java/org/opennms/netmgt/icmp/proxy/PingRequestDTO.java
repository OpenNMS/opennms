/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.rpc.api.RpcRequest;

@XmlRootElement(name="ping-request")
@XmlAccessorType(XmlAccessType.FIELD)
public class PingRequestDTO implements RpcRequest {

    @XmlElement(name="address")
    @XmlJavaTypeAdapter(value= InetAddressXmlAdapter.class)
    private InetAddress inetAddress;

    @XmlAttribute(name="location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name="retries")
    private int retries;

    @XmlAttribute(name="timeout")
    private long timeout;

    @XmlElement(name="packet-size")
    private int packetSize;

    @Override
    public String getLocation() {
        return location;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public Long getTimeToLiveMs() {
        return (1 + retries) * timeout * 2;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public void setRetries(int retries) {

        this.retries = retries;

    }

    public void setTimeout(long timeoutInMs) {
        this.timeout = timeoutInMs;

    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getRetries() {

        return retries;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public PingRequest toPingRequest() {
        final PingRequest pingRequest = new PingRequest();
        pingRequest.setInetAddress(inetAddress);
        pingRequest.setTimeout(timeout);
        pingRequest.setRetries(retries);
        pingRequest.setPacketSize(packetSize);
        return pingRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PingRequestDTO that = (PingRequestDTO) o;
        return Objects.equals(retries, that.retries) &&
                Objects.equals(timeout, that.timeout) &&
                Objects.equals(packetSize, that.packetSize) &&
                Objects.equals(inetAddress, that.inetAddress) &&
                Objects.equals(location, that.location) &&
                Objects.equals(systemId, that.systemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inetAddress, location, systemId, retries,
                timeout, packetSize);
    }
}
