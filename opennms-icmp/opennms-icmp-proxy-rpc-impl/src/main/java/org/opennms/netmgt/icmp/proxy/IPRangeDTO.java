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
