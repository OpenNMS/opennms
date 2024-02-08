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
package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.rpc.api.RpcRequest;

import io.opentracing.Span;

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

    private Map<String, String> tracingInfo = new HashMap<>();

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

    @Override
    public Map<String, String> getTracingInfo() {
        return tracingInfo;
    }

    @Override
    public Span getSpan() {
        return null;
    }

    public void addTracingInfo(String key, String value) {
        tracingInfo.put(key, value);
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
