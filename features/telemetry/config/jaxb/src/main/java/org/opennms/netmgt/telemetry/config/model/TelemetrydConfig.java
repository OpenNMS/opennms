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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "telemetryd-config")
@XmlAccessorType(XmlAccessType.NONE)
public class TelemetrydConfig {
    @XmlElement(name="listener")
    private List<ListenerConfig> listeners = new ArrayList<>();

    @XmlElement(name="connector")
    private List<ConnectorConfig> connectors = new ArrayList<>();

    @XmlElement(name="queue")
    private List<QueueConfig> queues = new ArrayList<>();

    public List<ListenerConfig> getListeners() {
        return this.listeners;
    }

    public void setListeners(final List<ListenerConfig> listeners) {
        this.listeners = listeners;
    }

    public List<ConnectorConfig> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<ConnectorConfig> connectors) {
        this.connectors = connectors;
    }

    public List<QueueConfig> getQueues() {
        return this.queues;
    }

    public void setQueues(final List<QueueConfig> queues) {
        this.queues = queues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TelemetrydConfig that = (TelemetrydConfig) o;

        return Objects.equals(this.listeners, that.listeners) &&
                Objects.equals(this.connectors, that.connectors) &&
                Objects.equals(this.queues, that.queues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.listeners, this.connectors, this.queues);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("listeners", this.listeners)
                .add("connectors", this.connectors)
                .add("queues", this.queues)
                .toString();
    }
}
