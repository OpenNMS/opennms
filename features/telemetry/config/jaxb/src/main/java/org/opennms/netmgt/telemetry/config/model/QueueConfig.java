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
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.telemetry.config.api.QueueDefinition;

import com.google.common.base.MoreObjects;

@XmlRootElement(name="queue")
@XmlAccessorType(XmlAccessType.NONE)
public class QueueConfig implements QueueDefinition {
    @XmlAttribute(name="name", required=true)
    @XmlID
    private String name;

    @XmlAttribute(name="threads")
    private Integer numThreads;

    @XmlAttribute(name="batch-size")
    private Integer batchSize;

    @XmlAttribute(name="batch-interval")
    private Integer batchIntervalMs;

    @XmlAttribute(name="queue-size")
    private Integer queueSize;

    @XmlElement(name="adapter")
    private List<AdapterConfig> adapters = new ArrayList<>();

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Optional<Integer> getNumThreads() {
        return Optional.ofNullable(this.numThreads);
    }

    public void setNumThreads(final Integer numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public Optional<Integer> getBatchSize() {
        return Optional.ofNullable(this.batchSize);
    }

    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public Optional<Integer> getBatchIntervalMs() {
        return Optional.ofNullable(this.batchIntervalMs);
    }

    public void setBatchIntervalMs(final Integer batchIntervalMs) {
        this.batchIntervalMs = batchIntervalMs;
    }

    @Override
    public Optional<Integer> getQueueSize() {
        return Optional.ofNullable(this.queueSize);
    }

    public void setQueueSize(final Integer queueSize) {
        this.queueSize = queueSize;
    }

    public List<AdapterConfig> getAdapters() {
        return this.adapters;
    }

    public void setAdapters(final List<AdapterConfig> adapters) {
        this.adapters = adapters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final QueueConfig that = (QueueConfig) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.numThreads, that.numThreads) &&
                Objects.equals(this.batchSize, that.batchSize) &&
                Objects.equals(this.batchIntervalMs, that.batchIntervalMs) &&
                Objects.equals(this.queueSize, that.queueSize) &&
                Objects.equals(this.adapters, that.adapters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.numThreads,
                this.batchSize,
                this.batchIntervalMs,
                this.queueSize,
                this.adapters);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("num-threads", this.numThreads)
                .add("batch-size", this.batchSize)
                .add("batch-interval-ms", this.batchIntervalMs)
                .add("queue-size", this.queueSize)
                .addValue(this.adapters)
                .toString();
    }
}
