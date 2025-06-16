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
