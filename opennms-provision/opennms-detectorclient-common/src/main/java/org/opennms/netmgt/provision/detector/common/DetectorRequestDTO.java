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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.common;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.camel.JaxbUtilsMarshalProcessor;
import org.opennms.core.camel.JaxbUtilsUnmarshalProcessor;
import org.opennms.core.network.InetAddressXmlAdapter;

@XmlRootElement(name = "detector-request")
@XmlAccessorType(XmlAccessType.NONE)
public class DetectorRequestDTO {

    public static class Marshal extends JaxbUtilsMarshalProcessor {
        public Marshal() {
            super(DetectorRequestDTO.class);
        }
    }

    public static class Unmarshal extends JaxbUtilsUnmarshalProcessor {
        public Unmarshal() {
            super(DetectorRequestDTO.class);
        }
    }

    @XmlAttribute(name = "location")
    private String location;

    @XmlAttribute(name = "class-name")
    private String className;

    @XmlAttribute(name = "address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress address;

    @XmlElement(name = "detector-attribute")
    private List<DetectorAttributeDTO> detectorAttributes = new ArrayList<>();

    @XmlElement(name = "agent-attribute")
    private List<DetectorAttributeDTO> agentAttributes = new ArrayList<>();

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<DetectorAttributeDTO> getDetectorAttributes() {
        return detectorAttributes;
    }

    public void setDetectorAttributes(List<DetectorAttributeDTO> attributes) {
        this.detectorAttributes = attributes;
    }

    public void addDetectorAttribute(String key, String value) {
        detectorAttributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addDetectorAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> this.addDetectorAttribute(e.getKey(), e.getValue()));
    }

    public Map<String, String> getAttributeMap() {
        return detectorAttributes.stream().collect(Collectors.toMap(DetectorAttributeDTO::getKey,
                DetectorAttributeDTO::getValue));
    }

    public void setAgentAttributes(List<DetectorAttributeDTO> attributes) {
        this.agentAttributes = attributes;
    }

    public void addAgentAttribute(String key, String value) {
        agentAttributes.add(new DetectorAttributeDTO(key, value));
    }

    public void addAgentAttributes(Map<String, String> attributes) {
        attributes.entrySet().stream()
            .forEach(e -> this.addAgentAttribute(e.getKey(), e.getValue()));
    }

    public Map<String, String> getAgentAttributeMap() {
        return agentAttributes.stream().collect(Collectors.toMap(DetectorAttributeDTO::getKey,
                DetectorAttributeDTO::getValue));
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, detectorAttributes, agentAttributes, className, address);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DetectorRequestDTO other = (DetectorRequestDTO) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.detectorAttributes, other.detectorAttributes)
                && Objects.equals(this.agentAttributes, other.agentAttributes)
                && Objects.equals(this.className, other.className)
                && Objects.equals(this.address, other.address);
    }
}
