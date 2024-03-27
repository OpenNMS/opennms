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
package org.opennms.netmgt.telemetry.config.model;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

import com.google.common.base.MoreObjects;

@XmlRootElement(name="adapter")
@XmlAccessorType(XmlAccessType.NONE)
public class AdapterConfig implements AdapterDefinition {

    @XmlTransient
    private QueueConfig queue;

    @XmlAttribute(name="name", required=true)
    private String name;

    @XmlAttribute(name="class-name", required=true)
    private String className;

    @XmlAttribute(name="enabled")
    private boolean enabled;

    @XmlElement(name="parameter")
    private List<Parameter> parameters = new ArrayList<>();

    @XmlElement(name="package")
    private List<PackageConfig> packages = new ArrayList<>();

    public QueueConfig getQueue() {
        return this.queue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @XmlTransient
    public String getFullName() {
        return String.format("%s.%s", this.queue.getName(), this.getName());
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public Map<String, String> getParameterMap() {
        return parameters.stream()
                .collect(Collectors.toMap(Parameter::getKey, Parameter::getValue));
    }

    @Override
    public List<PackageConfig> getPackages() {
        return this.packages;
    }

    public void setPackages(final List<PackageConfig> packages) {
        this.packages = packages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AdapterConfig that = (AdapterConfig) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.className, that.className) &&
                Objects.equals(this.enabled, that.enabled) &&
                Objects.equals(this.parameters, that.parameters) &&
                Objects.equals(this.packages, that.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.className,
                this.enabled,
                this.parameters,
                this.packages);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("class-name", this.className)
                .add("enabled", this.enabled)
                .addValue(this.parameters)
                .add("packages", this.packages)
                .toString();
    }

    public void afterUnmarshal(final Unmarshaller u, final Object parent) {
        this.queue = (QueueConfig) parent;
    }
}
