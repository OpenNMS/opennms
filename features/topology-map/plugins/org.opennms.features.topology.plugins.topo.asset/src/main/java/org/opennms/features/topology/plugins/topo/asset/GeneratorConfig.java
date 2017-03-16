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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeParamLabels;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class GeneratorConfig {

    private String label = "Asset Topology Provider";
    private String breadcrumbStrategy = BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT.name();
    private String providerId = "asset";
    private String preferredLayout = "Grid Layout";
    private String filter = "";
    private boolean generateUnallocated;
    private List<String> layerHierarchy = Lists.newArrayList(
            NodeParamLabels.ASSET_REGION,
            NodeParamLabels.ASSET_BUILDING,
            NodeParamLabels.ASSET_RACK);

    // TODO MVR make it bean compliant
    public boolean getGenerateUnallocated() {
		return generateUnallocated;
	}

	public void setGenerateUnallocated(boolean generateUnallocated) {
		this.generateUnallocated = generateUnallocated;
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
        return layerHierarchy;
    }

    public void setLayerHierarchies(List<String> layers) {
        this.layerHierarchy = layers;
    }

    public void setAssetLayers(String assetLayers) {
        if (assetLayers != null) {
            List<String> layers = Arrays.asList(assetLayers.split(",")).stream()
                    .filter(h -> h != null && !h.trim().isEmpty())
                    .map(h -> h.trim())
                    .collect(Collectors.toList());
            setLayerHierarchies(layers);
        }
    }

    public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
    public List<String> getFilterList() {
    	if(filter == null || filter.isEmpty()) return null;
        return Arrays.asList(filter.split("&")).stream()
                .filter(h -> h != null && !h.trim().isEmpty())
                .map(h -> h.trim())
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, breadcrumbStrategy, providerId, preferredLayout, filter, generateUnallocated, layerHierarchy);
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
                    && Objects.equals(filter, other.filter)
                    && Objects.equals(layerHierarchy, other.layerHierarchy);
        }
        return false;
    }

    @Override
	public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("providerId", providerId)
                .add("label", label)
                .add("layerHierarchy", layerHierarchy)
                .add("filter", filter)
                .add("breadcrumbStrategy", breadcrumbStrategy)
                .add("preferredLayout", preferredLayout)
                .add("generateUnallocated", generateUnallocated)
                .toString();
	}
}
