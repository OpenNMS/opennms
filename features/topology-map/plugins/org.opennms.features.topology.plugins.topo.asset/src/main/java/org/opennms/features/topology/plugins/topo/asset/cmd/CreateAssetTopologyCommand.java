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

package org.opennms.features.topology.plugins.topo.asset.cmd;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphMLProvider;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigBuilder;

@Command(scope = "asset-topology", name = "create", description="Creates Asset Topology. Uses default config if options are not supplied.")
@Service
public class CreateAssetTopologyCommand implements Action {

	@Reference
	public AssetGraphMLProvider assetGraphMLProvider;

	@Option(name = "-i", aliases =  "--providerId", description = "Unique providerId of asset topology", required = false, multiValued = false)
	String providerId;

	@Option(name = "-h", aliases = "--assetLayers", description = "Comma seperated list defining hierarchy of asset layers", required = false, multiValued = false)
	String hierarchy;

	@Option(name = "-f", aliases =  "--filter", description = "Optional node filter", required = false, multiValued = false)
	String filter;

	@Option(name = "-l", aliases = "--label", description = "Asset Topology label which shows in topology menu"
			+ " (if --providerId is specified it is used instead of the default label when --label is not set)", required = false, multiValued = false)
	String label;

	@Option(name = "-b", aliases ="--breadcrumbStrategy", description = "Bread Crumb Strategy", required = false, multiValued = false)
	String breadcrumbStrategy;

	@Option(name = "-p", aliases = "--preferredLayout", description = "Preferred Layout", required = false, multiValued = false)
	String preferredLayout;

	@Override
	public Object execute() {
		final GeneratorConfig generatorConfig = new GeneratorConfigBuilder()
			.withProviderId(providerId)
			.withHierarchy(hierarchy)
			.withLabel(label)
			.withBreadcrumbStrategy(breadcrumbStrategy)
			.withPreferredLayout(preferredLayout)
			.withFilters(filter)
			.build();

		// Build output
		System.out.println("Creating Asset Topology from configuration:");
		System.out.print(JaxbUtils.marshal(generatorConfig));

		assetGraphMLProvider.createAssetTopology(generatorConfig);
		System.out.println("Asset Topology created");
		return null;
	}

}
