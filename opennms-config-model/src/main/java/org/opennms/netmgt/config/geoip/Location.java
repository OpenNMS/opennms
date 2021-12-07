/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.geoip;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "location")
@XmlAccessorType(XmlAccessType.NONE)
public class Location {
    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlElement(name = "subnet")
    private List<Subnet> subnets = new ArrayList<>();

    public Location() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Subnet> getSubnets() {
        return subnets;
    }

    public void setSubnets(final List<Subnet> subnets) {
        this.subnets = subnets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(name, location.name) && Objects.equals(subnets, location.subnets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subnets);
    }

    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", subnets=" + subnets +
                '}';
    }
}
