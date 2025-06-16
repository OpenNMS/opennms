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
package org.opennms.netmgt.provision.dns.client.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RpcRequest;

import io.opentracing.Span;

@XmlRootElement(name = "dns-lookup-request")
@XmlAccessorType(XmlAccessType.NONE)
public class DnsLookupRequestDTO implements RpcRequest {

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlAttribute(name = "host-request")
    private String hostRequest;

    @XmlAttribute(name = "query-type")
    private QueryType queryType;

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
        return null;
    }

    public String getHostRequest() {
        return hostRequest;
    }

    public void setHostRequest(String hostRequest) {
        this.hostRequest = hostRequest;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public QueryType getQueryType() {
        return this.queryType == null ? QueryType.LOOKUP : this.queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
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
        return Objects.hash(location, systemId, hostRequest, queryType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DnsLookupRequestDTO other = (DnsLookupRequestDTO) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.systemId, other.systemId)
                && Objects.equals(this.hostRequest, other.hostRequest)
                && Objects.equals(this.queryType, other.queryType);
    }

}
