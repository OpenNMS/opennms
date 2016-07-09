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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.camel.JaxbUtilsMarshalProcessor;
import org.opennms.core.camel.JaxbUtilsUnmarshalProcessor;

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

    @XmlElement(name = "attributes")
    private Map<String, String> attributes = new HashMap<>();

    @XmlAttribute(name = "serviceName")
    private String serviceName;

    @XmlAttribute(name = "address")
    private String address;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, attributes, serviceName, address);
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
                && Objects.equals(this.attributes, other.attributes)
                && Objects.equals(this.serviceName, other.serviceName)
                && Objects.equals(this.address, other.address);

    }

}
