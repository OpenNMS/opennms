/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.web.rest.v2.bsm.model.edge.EdgeRequestDTO;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


@XmlRootElement(name = "business-service")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown=true)
public class BusinessServiceRequestDTO {

    @XmlElement(name = "name")
    private String m_name;

    @XmlElement(name = "attributes", required = false)
    @XmlJavaTypeAdapter(JAXBMapAdapter.class)
    private Map<String, String> m_attributes = Maps.newLinkedHashMap();

    @XmlElement(name="ip-service-edge")
    @XmlElementWrapper(name="ip-services-edges")
    private Set<EdgeRequestDTO<Integer>> m_ipServices = Sets.newLinkedHashSet();

    @XmlElement(name="child-edge")
    @XmlElementWrapper(name="child-edges")
    private Set<EdgeRequestDTO<Long>> m_childServices = Sets.newLinkedHashSet();

    @XmlElement(name="reductionkey-edge")
    @XmlElementWrapper(name="reductionkey-edges")
    private Set<EdgeRequestDTO<String>> m_reductionKeys = Sets.newHashSet();

    @XmlElement(name="reduce-function")
    private ReduceFunctionDTO m_reduceFunction;

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public Map<String, String> getAttributes() {
        return m_attributes;
    }

    protected void addAttribute(String key, String value) {
        getAttributes().put(key, value);
    }

    public void setAttributes(Map<String, String> attributes) {
        m_attributes = attributes;
    }

    public Set<EdgeRequestDTO<Integer>> getIpServices() {
        return m_ipServices;
    }

    public void setIpServices(Set<EdgeRequestDTO<Integer>> ipServices) {
        m_ipServices = ipServices;
    }

    public Set<EdgeRequestDTO<Long>> getChildServices() {
        return m_childServices;
    }

    public void setChildServices(Set<EdgeRequestDTO<Long>> childServices) {
        m_childServices = childServices;
    }

    public Set<EdgeRequestDTO<String>> getReductionKeys() {
        return m_reductionKeys;
    }

    public void setReductionKeys(Set<EdgeRequestDTO<String>> reductionKeys) {
        m_reductionKeys = reductionKeys;
    }

    public void setReduceFunction(ReduceFunctionDTO reduceFunction) {
        this.m_reduceFunction = reduceFunction;
    }

    public ReduceFunctionDTO getReduceFunction() {
        return m_reduceFunction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BusinessServiceRequestDTO other = (BusinessServiceRequestDTO) obj;

        return Objects.equals(m_name, other.m_name)
                && Objects.equals(m_attributes, other.m_attributes)
                && Objects.equals(m_reduceFunction, other.m_reduceFunction)
                && Objects.equals(m_ipServices, other.m_ipServices)
                && Objects.equals(m_childServices, other.m_childServices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_attributes, m_ipServices, m_childServices, m_reductionKeys, m_reduceFunction);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("name", m_name)
                .add("attributes", m_attributes)
                .add("reduceFunction", m_reduceFunction)
                .add("ipServices", m_ipServices)
                .add("childServices", m_childServices)
                .toString();
    }

    public void addChildService(long childId, MapFunctionDTO mapFunction) {
        EdgeRequestDTO<Long> edge = new EdgeRequestDTO<>();
        edge.setValue(childId);
        edge.setMapFunction(mapFunction);
        getChildServices().add(edge);
    }

    public void addReductionKey(String reductionKey, MapFunctionDTO mapFunction) {
        EdgeRequestDTO<String> edge = new EdgeRequestDTO<>();
        edge.setValue(reductionKey);
        edge.setMapFunction(mapFunction);
        getReductionKeys().add(edge);
    }

    public void addIpService(int ipServiceId, MapFunctionDTO mapFunction) {
        EdgeRequestDTO<Integer> edge = new EdgeRequestDTO<>();
        edge.setValue(ipServiceId);
        edge.setMapFunction(mapFunction);
        getIpServices().add(edge);
    }
}
