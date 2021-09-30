/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;

import com.google.common.base.MoreObjects;

@XmlAccessorType(XmlAccessType.NONE)
public class GeneratorConfig {

    @XmlElement(name="label")
    private String label = "Asset Topology Provider";

    @XmlElement(name="breadcrumb-strategy")
    private String breadcrumbStrategy = BreadcrumbStrategy.SHORTEST_PATH_TO_ROOT.name();

    @XmlID
    @XmlElement(name="provider-id")
    private String providerId = "asset";

    @XmlElement(name="preferred-layout")
    private String preferredLayout = "Grid Layout";

    @XmlElement(name="filter")
    @XmlElementWrapper(name="filters")
    private List<String> filters = new ArrayList<>();

    @XmlElementWrapper(name="layers")
    @XmlElement(name="layer")
    private List<String> layerHierarchies = new ArrayList<>();

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
        if (layers == this.layerHierarchies) return;
        this.layerHierarchies.clear();
        if (layers != null) this.layerHierarchies.addAll(layers);
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        if (filters == this.filters) return;
        this.filters.clear();
        if (filters != null) this.filters.addAll(filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, breadcrumbStrategy, providerId, filters, preferredLayout, layerHierarchies);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof GeneratorConfig) {
            GeneratorConfig other = (GeneratorConfig) obj;
            return Objects.equals(label, other.label)
                    && Objects.equals(breadcrumbStrategy, other.breadcrumbStrategy)
                    && Objects.equals(providerId, other.providerId)
                    && Objects.equals(filters, other.filters)
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
                .add("filters", filters)
                .add("breadcrumbStrategy", breadcrumbStrategy)
                .add("preferredLayout", preferredLayout)
                .toString();
	}
}
