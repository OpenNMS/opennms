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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "geoip-config")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("geoip-configuration.xsd")
public class GeoIpConfig {
    @XmlType
    @XmlEnum(String.class)
    public enum Resolve {
        @XmlEnumValue("primary") PRIMARY,
        @XmlEnumValue("public") PUBLIC,
        @XmlEnumValue("public-ipv4") PUBLIC_IPV4,
        @XmlEnumValue("public-ipv6") PUBLIC_IPV6
    }

    @XmlAttribute(name = "enabled", required = true)
    private boolean enabled = false;

    @XmlAttribute(name = "overwrite", required = false)
    private boolean overwrite = false;

    @XmlAttribute(name = "database", required = true)
    private String database = "";

    @XmlAttribute(name = "resolve", required = false)
    private Resolve resolve = Resolve.PUBLIC;

    @XmlElement(name = "location")
    private List<Location> locations = new ArrayList<>();

    public GeoIpConfig() {
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(final List<Location> locations) {
        this.locations = locations;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(final String database) {
        this.database = database;
    }

    public Resolve getResolve() {
        return resolve;
    }

    public void setResolve(final Resolve resolve) {
        this.resolve = resolve;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public String toString() {
        return "GeoIpConfig{" +
                "enabled=" + enabled +
                ", overwrite=" + overwrite +
                ", database='" + database + '\'' +
                ", resolve='" + resolve + '\'' +
                ", locations=" + locations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoIpConfig that = (GeoIpConfig) o;
        return enabled == that.enabled && overwrite == that.overwrite && Objects.equals(database, that.database) && Objects.equals(resolve, that.resolve) && Objects.equals(locations, that.locations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, overwrite, database, resolve, locations);
    }
}
