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
package org.opennms.netmgt.provision.persist.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.netmgt.provision.persist.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionRequest;

import io.opentracing.Span;

@XmlRootElement(name = "requisition-request")
@XmlAccessorType(XmlAccessType.NONE)
public class RequisitionRequestDTO implements RpcRequest {

    @XmlAttribute(name = "type")
    private String type;

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name="system-id")
    private String systemId;

    @XmlValue
    @XmlCDATA
    private String marshaledProviderRequest;

    private RequisitionRequest providerRequest;

    private Long timeToLiveMs;

    private Map<String, String> tracingInfo = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
        return timeToLiveMs;
    }

    public void setTimeToLiveMs(Long timeToLiveMs) {
        this.timeToLiveMs = timeToLiveMs;
    }

    public RequisitionRequest getProviderRequest(RequisitionProvider provider) {
        if (providerRequest != null) {
            return providerRequest;
        }
        return provider.unmarshalRequest(marshaledProviderRequest);
    }

    public void setProviderRequest(RequisitionRequest providerRequest) {
        this.providerRequest = providerRequest;
    }

    public void setProviderRequest(String marshaledProviderRequest) {
        this.marshaledProviderRequest = marshaledProviderRequest;
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
    public boolean equals(final Object other) {
        if (!(other instanceof RequisitionRequestDTO)) {
            return false;
        }
        RequisitionRequestDTO castOther = (RequisitionRequestDTO) other;
        return Objects.equals(location, castOther.location)
                && Objects.equals(systemId, castOther.systemId)
                && Objects.equals(timeToLiveMs, castOther.timeToLiveMs)
                && Objects.equals(type, castOther.type)
                && Objects.equals(providerRequest, castOther.providerRequest)
                && Objects.equals(marshaledProviderRequest, castOther.marshaledProviderRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, systemId, timeToLiveMs, type,
                providerRequest, marshaledProviderRequest);
    }

}
