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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.opennms.web.rest.v2.bsm.model.edge.AbstractEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeRequestDTO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


@XmlRootElement(name = "business-service")
@XmlAccessorType(XmlAccessType.NONE)
public class BusinessServiceRequestDTO {

    @XmlElement(name = "name")
    private String m_name;

    @XmlElement(name = "attributes", required = false)
    @XmlJavaTypeAdapter(JAXBMapAdapter.class)
    private Map<String, String> m_attributes = Maps.newLinkedHashMap();

    @XmlElement(name="ip-service-edge")
    @XmlElementWrapper(name="ip-service-edges")
    private List<IpServiceEdgeRequestDTO> m_ipServices = Lists.newArrayList();

    @XmlElement(name="child-edge")
    @XmlElementWrapper(name="child-edges")
    private List<ChildEdgeRequestDTO> m_childServices = Lists.newArrayList();

    @XmlElement(name="reduction-key-edge")
    @XmlElementWrapper(name="reduction-key-edges")
    private List<ReductionKeyEdgeRequestDTO> reductionKeys = Lists.newArrayList();

    @XmlElement(name="reduce-function")
    private ReduceFunctionDTO reduceFunction;

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

    public List<IpServiceEdgeRequestDTO> getIpServices() {
        return m_ipServices;
    }

    public void setIpServices(List<IpServiceEdgeRequestDTO> ipServices) {
        m_ipServices = ipServices;
    }

    public List<ChildEdgeRequestDTO> getChildServices() {
        return m_childServices;
    }

    public void setChildServices(List<ChildEdgeRequestDTO> childServices) {
        m_childServices = childServices;
    }

    public List<ReductionKeyEdgeRequestDTO> getReductionKeys() {
        return reductionKeys;
    }

    public void setReductionKeys(List<ReductionKeyEdgeRequestDTO> reductionKeys) {
        this.reductionKeys = reductionKeys;
    }

    public void setReduceFunction(ReduceFunctionDTO reduceFunction) {
        this.reduceFunction = reduceFunction;
    }

    public ReduceFunctionDTO getReduceFunction() {
        return reduceFunction;
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
                && Objects.equals(reduceFunction, other.reduceFunction)
                && Objects.equals(m_ipServices, other.m_ipServices)
                && Objects.equals(m_childServices, other.m_childServices)
                && Objects.equals(reductionKeys, other.reductionKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_attributes, m_ipServices, m_childServices, reductionKeys, reduceFunction);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("name", m_name)
                .add("attributes", m_attributes)
                .add("reduceFunction", reduceFunction)
                .add("ipServices", m_ipServices)
                .add("childServices", m_childServices)
                .add("reductionKeys", reductionKeys)
                .toString();
    }

    public void addChildService(long childId, MapFunctionDTO mapFunction, int weight) {
        ChildEdgeRequestDTO edge = new ChildEdgeRequestDTO();
        edge.setChildId(childId);
        edge.setMapFunction(mapFunction);
        edge.setWeight(weight);
        getChildServices().add(edge);
    }

    public void addReductionKey(String reductionKey, MapFunctionDTO mapFunction, int weight) {
        addReductionKey(reductionKey, mapFunction, weight, null);
    }

    public void addReductionKey(String reductionKey, MapFunctionDTO mapFunction, int weight, String friendlyName) {
        ReductionKeyEdgeRequestDTO edge = new ReductionKeyEdgeRequestDTO();
        edge.setReductionKey(reductionKey);
        edge.setMapFunction(mapFunction);
        edge.setWeight(weight);
        edge.setFriendlyName(friendlyName);
        getReductionKeys().add(edge);
    }

    public void addIpService(int ipServiceId, MapFunctionDTO mapFunction, int weight) {
        addIpService(ipServiceId, mapFunction, weight, null);
    }

    public void addIpService(int ipServiceId, MapFunctionDTO mapFunction, int weight, String friendlyName) {
        IpServiceEdgeRequestDTO edge = new IpServiceEdgeRequestDTO();
        edge.setIpServiceId(ipServiceId);
        edge.setMapFunction(mapFunction);
        edge.setWeight(weight);
        edge.setFriendlyName(friendlyName);
        getIpServices().add(edge);
    }

    @XmlTransient
    @JsonIgnore
    public List<AbstractEdgeRequestDTO> getEdges() {
        List<AbstractEdgeRequestDTO> edges = new ArrayList<>();
        edges.addAll(getChildServices());
        edges.addAll(getIpServices());
        edges.addAll(getReductionKeys());
        return edges;
    }
}
