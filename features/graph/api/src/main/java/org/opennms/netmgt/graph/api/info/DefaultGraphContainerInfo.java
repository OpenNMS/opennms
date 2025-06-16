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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultGraphContainerInfo implements GraphContainerInfo {

    private final String id;
    private List<GraphInfo> graphInfos = new ArrayList<>();
    private String description;
    private String label;

    public DefaultGraphContainerInfo(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public List<String> getNamespaces() {
        return graphInfos.stream().map(gi -> gi.getNamespace()).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        Objects.requireNonNull(namespace);
        return graphInfos.stream().filter(gi -> namespace.equals(gi.getNamespace())).findFirst().orElse(null);
    }

    public void addGraphInfo(GraphInfo graphInfo) {
        graphInfos.add(graphInfo);
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return graphInfos.get(0);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<GraphInfo> getGraphInfos() {
        return graphInfos;
    }

    public void setGraphInfos(List<GraphInfo> graphInfos) {
        this.graphInfos = graphInfos;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, graphInfos, description, label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGraphContainerInfo that = (DefaultGraphContainerInfo) o;
        return Objects.equals(id, that.id)
                && Objects.equals(graphInfos, that.graphInfos)
                && Objects.equals(description, that.description)
                && Objects.equals(label, that.label);
    }
}
