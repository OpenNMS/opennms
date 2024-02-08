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

