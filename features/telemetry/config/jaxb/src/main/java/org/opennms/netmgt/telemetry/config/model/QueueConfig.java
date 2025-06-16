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

    @XmlAttribute(name="use-routing-key")
    private Boolean useRoutingKey;

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

    @Override
    public Optional<Boolean> getUseRoutingKey() {
        return Optional.ofNullable(useRoutingKey);
    }

    public void setUseRoutingKey(Boolean useRoutingKey) {
        this.useRoutingKey = useRoutingKey;
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
                Objects.equals(this.useRoutingKey, that.useRoutingKey) &&
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
                this.useRoutingKey,
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
                .add("use-routing-key", this.useRoutingKey)
                .addValue(this.adapters)
                .toString();
    }
}
