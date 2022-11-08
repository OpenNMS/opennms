/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
