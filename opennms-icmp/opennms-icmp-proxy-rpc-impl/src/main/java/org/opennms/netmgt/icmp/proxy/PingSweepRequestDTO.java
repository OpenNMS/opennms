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

import static java.math.MathContext.DECIMAL64;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.PingConstants;

import io.opentracing.Span;

@XmlRootElement(name = "ping-sweep-request")
@XmlAccessorType(XmlAccessType.FIELD)
public class PingSweepRequestDTO implements RpcRequest {

    private static final BigDecimal FUDGE_FACTOR = BigDecimal.valueOf(1.5);

    @XmlElement(name = "ip-range")
    private List<IPRangeDTO> ipRanges = new ArrayList<>();

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name = "packet-size")
    private Integer packetSize;

    @XmlAttribute(name = "packets-per-second")
    private Double packetsPerSecond;

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

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPacketSize() {
        return packetSize != null ? packetSize : PingConstants.DEFAULT_PACKET_SIZE;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public double getPacketsPerSecond() {
        return packetsPerSecond != null ? packetsPerSecond : PingConstants.DEFAULT_PACKETS_PER_SECOND;
    }

    public void setPacketsPerSecond(double packetsPerSecond) {
        this.packetsPerSecond = packetsPerSecond;
    }

    public List<IPRangeDTO> getIpRanges() {
        return ipRanges;
    }

    public void addIpRange(IPRangeDTO range) {
        ipRanges.add(range);
    }

    public void setIpRanges(List<IPRangeDTO> ipRanges) {
        this.ipRanges = ipRanges;
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

    @Override
    public int hashCode() {
        return Objects.hash(ipRanges, location, systemId, packetSize);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PingSweepRequestDTO other = (PingSweepRequestDTO) obj;
        return Objects.equals(this.ipRanges, other.ipRanges)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.systemId, other.systemId)
                && Objects.equals(this.packetSize, other.packetSize)
                && Objects.equals(this.packetsPerSecond, other.packetsPerSecond);
    }

    @Override
    public Long getTimeToLiveMs() {
        BigDecimal taskTimeOut = BigDecimal.ZERO;
        for (final IPRangeDTO range : ipRanges) {
            BigInteger size = InetAddressUtils.difference(InetAddressUtils.getInetAddress(range.getEnd().getAddress()),
                    InetAddressUtils.getInetAddress(range.getBegin().getAddress())).add(BigInteger.ONE);
            taskTimeOut = taskTimeOut.add(
                    // Take the number of retries
                    BigDecimal.valueOf(range.getRetries())
                            // Add 1 for the original request
                            .add(BigDecimal.ONE, DECIMAL64)
                            // Multiply by the number of addresses
                            .multiply(new BigDecimal(size), DECIMAL64)
                            // Multiply by the timeout per retry
                            .multiply(BigDecimal.valueOf(range.getTimeout()), DECIMAL64)
                            // Multiply by the fudge factor
                            .multiply(FUDGE_FACTOR, DECIMAL64),
                    DECIMAL64);

            // Add a delay for the rate limiting done with the
            // packetsPerSecond field
            taskTimeOut = taskTimeOut.add(
                    // Take the number of addresses
                    new BigDecimal(size)
                            // Divide by the number of packets per second
                            .divide(BigDecimal.valueOf(getPacketsPerSecond()), DECIMAL64)
                            // 1000 milliseconds
                            .multiply(BigDecimal.valueOf(1000), DECIMAL64),
                    DECIMAL64);
        }
        // If the timeout is greater than Long.MAX_VALUE, just return Long.MAX_VALUE
        return taskTimeOut.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) >= 0 ? Long.MAX_VALUE
                : taskTimeOut.longValue();
    }

}
