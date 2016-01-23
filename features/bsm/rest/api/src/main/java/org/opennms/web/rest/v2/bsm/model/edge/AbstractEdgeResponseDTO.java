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

public abstract class AbstractEdgeResponseDTO {

    @XmlElement(name="id")
    private long m_id;

    @XmlElement(name="operational-status")
    private Status m_operationalStatus;

    @XmlElement(name="map-function")
    private MapFunctionDTO m_mapFunction;

    @XmlElement(name="location")
    @XmlJavaTypeAdapter(JAXBResourceLocationAdapter.class)
    @JsonSerialize(using = JsonResourceLocationSerializationProvider.class)
    @JsonDeserialize(using = JsonResourceLocationDeserializationProvider.class)
    private ResourceLocation location;

    @XmlElement(name="reductionKey")
    @XmlElementWrapper(name="reductionKeys")
    private Set<String> m_reductionKeys = new HashSet<>();

    public Status getOperationalStatus() {
        return this.m_operationalStatus;
    }

    public void setOperationalStatus(final Status operationalStatus) {
        this.m_operationalStatus = operationalStatus;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public long getId() {
        return m_id;
    }

    public void setId(long id) {
        this.m_id = id;
    }

    public Set<String> getReductionKeys() {
        return m_reductionKeys;
    }

    public void setReductionKeys(Set<String> reductionKeys) {
        m_reductionKeys = reductionKeys;
    }

    public void setMapFunction(MapFunctionDTO mapFunction) {
        this.m_mapFunction = mapFunction;
    }

    public MapFunctionDTO getMapFunction() {
        return m_mapFunction;
    }
}
