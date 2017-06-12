/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.rpc;

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
