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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.model.edge;

import java.util.HashSet;
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

import com.google.common.base.Objects;

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
        final boolean equals = Objects.equal(id, other.id)
                && Objects.equal(operationalStatus, other.operationalStatus)
                && Objects.equal(mapFunction, other.mapFunction)
                && Objects.equal(reductionKeys, other.reductionKeys)
                && Objects.equal(weight, other.weight)
                && Objects.equal(location, other.location);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id,
                operationalStatus,
                mapFunction,
                reductionKeys,
                weight,
                location);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("operationalStatus", operationalStatus)
                .add("weight", weight)
                .add("mapFunction", mapFunction)
                .add("location", location)
                .add("reductionKeys", reductionKeys)
                .toString();
    }
}
