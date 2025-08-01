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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.events.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorConfigBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(GeneratorConfigBuilder.class);

	private String label;
	private String breadcrumbStrategy;
	private String providerId;
	private String preferredLayout;
	private String hierarchy = String.join(",", NodeParamLabels.ASSET_REGION,
	            NodeParamLabels.ASSET_BUILDING,
	            NodeParamLabels.ASSET_RACK);
	private String filter;

	public GeneratorConfigBuilder withLabel(String label) {
		this.label = label;
		return this;
	}

	public GeneratorConfigBuilder withBreadcrumbStrategy(String breadcrumbStrategy) {
		this.breadcrumbStrategy = breadcrumbStrategy;
		return this;
	}

	public GeneratorConfigBuilder withProviderId(String providerId) {
		this.providerId = providerId;
		return this;
	}

	public GeneratorConfigBuilder withPreferredLayout(String preferredLayout) {
		this.preferredLayout = preferredLayout;
		return this;
	}

	public GeneratorConfigBuilder withHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
		return this;
	}

	public GeneratorConfigBuilder withFilters(String filter) {
		this.filter = filter;
		return this;
	}

	public GeneratorConfig build() {
		final GeneratorConfig config = new GeneratorConfig();
		if (label != null) {
			config.setLabel(label);
		}
		if (providerId != null) {
			config.setProviderId(providerId);
			// this avoids using a duplicate default label by mistake
			// if a label is not provided with new providerId
			if(label ==null) config.setLabel(providerId);
		}
		if (preferredLayout != null) {
			config.setPreferredLayout(preferredLayout);
		}
		if (hierarchy != null && !hierarchy.trim().isEmpty()) {
			final List<String> layers = Arrays.asList(hierarchy.split(",")).stream()
					.filter(Objects::nonNull)
					.map(String::trim)
					.filter(h -> !h.isEmpty())
					.collect(Collectors.toList());
			config.setLayerHierarchies(layers);
		}

		if(filter != null && !filter.trim().isEmpty()) {
			final List<String> filters = Arrays.asList(filter.split(";")).stream()
					.filter(Objects::nonNull)
					.map(String::trim)
					.filter(h -> !h.isEmpty())
					.collect(Collectors.toList());
			config.setFilters(filters);
		}
		if (breadcrumbStrategy != null) {
			try {
				BreadcrumbStrategy validValue = BreadcrumbStrategy.valueOf(breadcrumbStrategy);
				config.setBreadcrumbStrategy(validValue.name());
			} catch (IllegalArgumentException ex) {
				LOG.warn("Given breadcrumbStrategy {} does not exist. Valid values are {}. Ignoring.", breadcrumbStrategy, BreadcrumbStrategy.values());
			}
		}
		return config;
	}

	public static GeneratorConfig buildFrom(IEvent e) {
		final String label = EventUtils.getParm(e, EventParameterNames.LABEL);
		final String breadcrumbStrategy = EventUtils.getParm(e, EventParameterNames.BREADCRUMB_STRATEGY);
		final String providerId = EventUtils.getParm(e, EventParameterNames.PROVIDER_ID);
		final String filters = EventUtils.getParm(e, EventParameterNames.FILTERS);
		final String preferredLayout = EventUtils.getParm(e, EventParameterNames.PREFERRED_LAYOUT);
		final String hierarchy = EventUtils.getParm(e, EventParameterNames.HIERARCHY);

		return new GeneratorConfigBuilder()
		.withLabel(label)
		.withBreadcrumbStrategy(breadcrumbStrategy)
		.withProviderId(providerId)
		.withPreferredLayout(preferredLayout)
		.withHierarchy(hierarchy)
		.withFilters(filters)
		.build();
	}
}
