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

package org.opennms.features.topology.plugins.topo.asset;

import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class GeneratorConfig {

    private String label = "Asset Topology Provider";
    private String breadcrumbStrategy = BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT.name();
    private String providerId = "asset";
    private String preferredLayout = "Grid Layout";
    private List<String> filters;
    private boolean generateUnallocated;
    private List<String> layerHierarchies = Lists.newArrayList(
            NodeParamLabels.ASSET_REGION,
            NodeParamLabels.ASSET_BUILDING,
            NodeParamLabels.ASSET_RACK);

    public void setGenerateUnallocated(boolean generateUnallocated) {
		this.generateUnallocated = generateUnallocated;
	}

    public boolean isGenerateUnallocated() {
        return generateUnallocated;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBreadcrumbStrategy() {
        return breadcrumbStrategy;
    }

    public void setBreadcrumbStrategy(String breadcrumbStrategy) {
        this.breadcrumbStrategy = breadcrumbStrategy;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getPreferredLayout() {
        return preferredLayout;
    }

    public void setPreferredLayout(String preferredLayout) {
        this.preferredLayout = preferredLayout;
    }

    public List<String> getLayerHierarchies() {
        return layerHierarchies;
    }

    public void setLayerHierarchies(List<String> layers) {
        this.layerHierarchies = layers;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getFilters() {
        return filters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, breadcrumbStrategy, providerId, preferredLayout, generateUnallocated, layerHierarchies);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof GeneratorConfig) {
            GeneratorConfig other = (GeneratorConfig) obj;
            return Objects.equals(generateUnallocated, other.generateUnallocated)
                    && Objects.equals(label, other.label)
                    && Objects.equals(breadcrumbStrategy, other.breadcrumbStrategy)
                    && Objects.equals(providerId, other.providerId)
                    && Objects.equals(preferredLayout, other.preferredLayout)
                    && Objects.equals(layerHierarchies, other.layerHierarchies);
        }
        return false;
    }

    @Override
	public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("providerId", providerId)
                .add("label", label)
                .add("layerHierarchies", layerHierarchies)
                .add("filter", filters)
                .add("breadcrumbStrategy", breadcrumbStrategy)
                .add("preferredLayout", preferredLayout)
                .add("generateUnallocated", generateUnallocated)
                .toString();
	}
}
