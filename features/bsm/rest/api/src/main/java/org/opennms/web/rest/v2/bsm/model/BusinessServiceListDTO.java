/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm.model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.opennms.web.rest.api.support.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.support.JsonResourceLocationListDeserializationProvider;
import org.opennms.web.rest.api.support.JsonResourceLocationListSerializationProvider;

@XmlRootElement(name = "business-services")
@JsonRootName("business-services")
public class BusinessServiceListDTO {
    private List<ResourceLocation> services;

    public BusinessServiceListDTO() {
    }

    public BusinessServiceListDTO(final Collection<? extends BusinessService> services) {
        this.services = services.stream()
        .map(service -> ResourceLocationFactory.createBusinessServiceLocation(Long.toString(service.getId())))
        .collect(Collectors.toList());
    }

    @XmlElement(name = "business-service")
    @JsonProperty("business-services")
    @JsonSerialize(using = JsonResourceLocationListSerializationProvider.class)
    @JsonDeserialize(using = JsonResourceLocationListDeserializationProvider.class)
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    public List<ResourceLocation> getServices() {
        return this.services;
    }

    public void setServices(final List<ResourceLocation> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessServiceListDTO other = (BusinessServiceListDTO) obj;

        return Objects.equals(services, other.services);
    }

    @Override
    public int hashCode() {
        return Objects.hash(services);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("services", services)
                .toString();
    }
}
