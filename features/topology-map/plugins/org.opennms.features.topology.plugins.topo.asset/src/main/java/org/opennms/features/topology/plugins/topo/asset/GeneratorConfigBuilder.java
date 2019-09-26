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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
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

	public static GeneratorConfig buildFrom(Event e) {
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
