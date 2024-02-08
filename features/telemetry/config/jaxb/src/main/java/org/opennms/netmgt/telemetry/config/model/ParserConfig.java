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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.telemetry.config.api.ParserDefinition;

import com.google.common.base.MoreObjects;

@XmlRootElement(name="parser")
@XmlAccessorType(XmlAccessType.NONE)
public class ParserConfig implements ParserDefinition {

    @XmlTransient
    private ListenerConfig listener;

    @XmlAttribute(name="name", required=true)
    private String name;

    @XmlAttribute(name="class-name", required=true)
    private String className;

    @XmlAttribute(name="queue", required=true)
    @XmlIDREF()
    private QueueConfig queue;

    @XmlElement(name="parameter")
    private List<Parameter> parameters = new ArrayList<>();

    public ListenerConfig getListener() {
        return this.listener;
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

    @Override
    @XmlTransient
    public String getFullName() {
        return String.format("%s.%s", this.listener.getName(), this.getName());
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ParserConfig that = (ParserConfig) o;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.className, that.className) &&
                Objects.equals(this.queue, that.queue) &&
                Objects.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.className, this.queue, this.parameters);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("class-name", this.className)
                .add("queue", this.queue.getName())
                .add("parameters", this.parameters)
                .toString();
    }

    public void afterUnmarshal(final Unmarshaller u, final Object parent) {
        this.listener = (ListenerConfig) parent;
    }
}
