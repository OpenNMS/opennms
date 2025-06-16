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
