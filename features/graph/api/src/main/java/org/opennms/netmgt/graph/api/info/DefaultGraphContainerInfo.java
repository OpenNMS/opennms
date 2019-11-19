/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
