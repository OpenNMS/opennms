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

package org.opennms.karaf.extender;

import java.util.Objects;

public class Feature {
    private final String name;
    private final String version;
    private final String karDependency;

    private Feature(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.karDependency = builder.karDependency;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String version;
        private String karDependency;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withKarDependency(String karDependency) {
            this.karDependency = karDependency;
            return this;
        }

        public Feature build() {
            Objects.requireNonNull(name, "name is required");
            return new Feature(this);
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getKarDependency() {
        return karDependency;
    }

    public String toInstallString() {
        return getVersion() != null ? getName() + "/" + getVersion() : getName();
    }

    @Override
    public String toString() {
        return String.format("Feature[name=%s, version=%s, karDependency=%s]", name, version, karDependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, karDependency);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Feature other = (Feature) obj;
        return Objects.equals(this.name, other.name) &&
                Objects.equals(this.version, other.version) &&
                Objects.equals(this.karDependency, other.karDependency);
    }
}

