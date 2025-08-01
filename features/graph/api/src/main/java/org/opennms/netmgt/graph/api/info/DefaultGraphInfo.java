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
package org.opennms.netmgt.graph.api.info;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class DefaultGraphInfo implements GraphInfo {

    private String namespace;
    private String description;
    private String label;

    public DefaultGraphInfo(final String namespace) {
        this.namespace = Objects.requireNonNull(namespace);
    }

    /**
     * Constructor to change the vertex Type of the given GraphInfo.
     */
    public DefaultGraphInfo(final GraphInfo copy) {
        this(Objects.requireNonNull(copy).getNamespace());
        setLabel(copy.getLabel());
        setDescription(copy.getDescription());
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DefaultGraphInfo withLabel(String label) {
        setLabel(label);
        return this;
    }

    public DefaultGraphInfo withDescription(String description) {
        setDescription(description);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGraphInfo graphInfo = (DefaultGraphInfo) o;
        return Objects.equals(namespace, graphInfo.namespace)
                && Objects.equals(description, graphInfo.description)
                && Objects.equals(label, graphInfo.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, description, label);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("namespace", namespace)
                .add("label", label)
                .add("description", description)
                .toString();
    }
}
