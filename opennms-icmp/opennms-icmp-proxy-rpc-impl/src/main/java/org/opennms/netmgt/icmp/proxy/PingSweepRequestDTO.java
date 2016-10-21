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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opennms.core.rpc.api.RpcRequest;

@XmlRootElement(name = "ping-sweep-request")
@XmlAccessorType(XmlAccessType.FIELD)
public class PingSweepRequestDTO implements RpcRequest {

    @XmlElement(name = "ip-ranges")
    private List<IPRangeDTO> ipRanges = new ArrayList<>();

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name = "retries")
    private int retries;

    @XmlAttribute(name = "timeout")
    private long timeout;

    @XmlElement(name = "packet-size")
    private int packetSize;

    @XmlElement(name = "foreign-Source")
    private String foreignSource;
    
    private Long ttl;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
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
    public int hashCode() {
        return Objects.hash(ipRanges, location, packetSize, retries, timeout);
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
        return Objects.equals(this.ipRanges, other.ipRanges) && Objects.equals(this.location, other.location)
                && Objects.equals(this.packetSize, other.packetSize) && Objects.equals(this.retries, other.retries)
                && Objects.equals(this.timeout, other.timeout);
    }

    @Override
    public Long getTimeToLiveMs() {
        return this.ttl;
    }
    

    public void setTimeToLiveMs(Long ttl) {
        this.ttl = ttl;
    }

}
