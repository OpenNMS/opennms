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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.telemetry.config.api.ConnectorDefinition;

import com.google.common.base.MoreObjects;

@XmlRootElement(name="connector")
@XmlAccessorType(XmlAccessType.NONE)
public class ConnectorConfig implements ConnectorDefinition {
    @XmlAttribute(name="name", required=true)
    private String name;

    @XmlAttribute(name="class-name", required=true)
    private String className;

    @XmlAttribute(name="service-name", required=true)
    private String serviceName;

    @XmlAttribute(name="queue", required=true)
    @XmlIDREF()
    private QueueConfig queue;

    @XmlAttribute(name="enabled")
    private boolean enabled;

    @XmlElement(name="parameter")
    private List<Parameter> parameters = new ArrayList<>();

    @XmlElement(name="package")
    private List<PackageConfig> packages = new ArrayList<>();

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

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public QueueConfig getQueue() {
        return this.queue;
    }

    public void setQueue(final QueueConfig queue) {
        this.queue = queue;
    }

    @Override
    @XmlTransient
    public String getQueueName() {
        if (queue != null) {
            return queue.getName();
        }
        return null;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
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
        final ConnectorConfig that = (ConnectorConfig) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.className, that.className) &&
                Objects.equals(this.serviceName, that.serviceName) &&
                Objects.equals(this.queue, that.queue) &&
                Objects.equals(this.enabled, that.enabled) &&
                Objects.equals(this.parameters, that.parameters) &&
                Objects.equals(this.packages, that.packages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.className,
                this.serviceName,
                this.queue,
                this.enabled,
                this.parameters,
                this.packages);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("class-name", this.className)
                .add("service-name", this.serviceName)
                .add("queue", this.queue)
                .add("enabled", this.enabled)
                .addValue(this.parameters)
                .add("packages", this.packages)
                .toString();
    }
}
