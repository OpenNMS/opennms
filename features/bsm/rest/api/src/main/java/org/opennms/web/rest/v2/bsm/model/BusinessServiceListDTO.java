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
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("services", services)
                .toString();
    }
}
