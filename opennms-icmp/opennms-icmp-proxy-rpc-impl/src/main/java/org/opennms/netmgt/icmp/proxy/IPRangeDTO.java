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
import java.net.UnknownHostException;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;

@XmlRootElement(name = "ip-range")
@XmlAccessorType(XmlAccessType.FIELD)
public class IPRangeDTO {

    @XmlAttribute(name = "begin")
    @XmlJavaTypeAdapter(value = InetAddressXmlAdapter.class)
    private InetAddress begin;

    @XmlAttribute(name = "end")
    @XmlJavaTypeAdapter(value = InetAddressXmlAdapter.class)
    private InetAddress end;

    @XmlAttribute(name = "retries")
    private int retries;

    @XmlAttribute(name = "timeout")
    private long timeout;

    public IPRangeDTO() {
    }

    public IPRangeDTO(InetAddress begin, InetAddress end, int retries, long timeout) {
        this.begin = begin;
        this.end = end;
        this.retries = retries;
        this.timeout = timeout;
    }

    public IPRangeDTO(String begin, String end, int retries, int timeout) throws UnknownHostException {
        this(InetAddress.getByName(begin), InetAddress.getByName(end), retries, timeout);
    }

    public InetAddress getBegin() {
        return begin;
    }

    public void setBegin(InetAddress begin) {
        this.begin = begin;
    }

    public InetAddress getEnd() {
        return end;
    }

    public void setEnd(InetAddress end) {
        this.end = end;
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

    @Override
    public int hashCode() {
        return Objects.hash(begin, end, retries, timeout);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IPRangeDTO other = (IPRangeDTO) obj;
        return Objects.equals(this.begin, other.begin)
                && Objects.equals(this.end, other.end)
                && Objects.equals(this.retries, other.retries)
                && Objects.equals(this.timeout, other.timeout);
    }

}
