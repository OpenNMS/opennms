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
package org.opennms.web.rest.v2.bsm.model.edge;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.api.support.JAXBResourceLocationAdapter;
import org.opennms.web.rest.api.support.JsonResourceLocationDeserializationProvider;
import org.opennms.web.rest.api.support.JsonResourceLocationSerializationProvider;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;

import com.google.common.base.MoreObjects;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractEdgeResponseDTO {

    @XmlElement(name="id")
    private long id;

    @XmlElement(name="operational-status")
    private Status operationalStatus;

    @XmlElement(name="map-function")
    private MapFunctionDTO mapFunction;

    @XmlElement(name="location")
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    @JsonSerialize(using = JsonResourceLocationSerializationProvider.class)
    @JsonDeserialize(using = JsonResourceLocationDeserializationProvider.class)
    private ResourceLocation location;

    @XmlElement(name="reduction-key")
    @XmlElementWrapper(name="reduction-keys")
    private Set<String> reductionKeys = new HashSet<>();

    @XmlElement(name="weight", required = true)
    private int weight = 1;

    public Status getOperationalStatus() {
        return this.operationalStatus;
    }

    public void setOperationalStatus(final Status operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    public void setReductionKeys(Set<String> reductionKeys) {
        this.reductionKeys = reductionKeys;
    }

    public void setMapFunction(MapFunctionDTO mapFunction) {
        this.mapFunction = mapFunction;
    }

    public MapFunctionDTO getMapFunction() {
        return mapFunction;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof AbstractEdgeResponseDTO)) return false;
        final AbstractEdgeResponseDTO other = (AbstractEdgeResponseDTO) obj;
        final boolean equals = Objects.equals(id, other.id)
                && Objects.equals(operationalStatus, other.operationalStatus)
                && Objects.equals(mapFunction, other.mapFunction)
                && Objects.equals(reductionKeys, other.reductionKeys)
                && Objects.equals(weight, other.weight)
                && Objects.equals(location, other.location);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                operationalStatus,
                mapFunction,
                reductionKeys,
                weight,
                location);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("operationalStatus", operationalStatus)
                .add("weight", weight)
                .add("mapFunction", mapFunction)
                .add("location", location)
                .add("reductionKeys", reductionKeys)
                .toString();
    }
}
