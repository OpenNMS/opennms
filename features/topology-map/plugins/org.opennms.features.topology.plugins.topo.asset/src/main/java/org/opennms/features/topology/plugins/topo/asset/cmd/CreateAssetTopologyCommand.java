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


import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphMLProvider;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "asset-topology", name = "create", description="Creates Asset Topology. Uses default config if options are not supplied.")
public class CreateAssetTopologyCommand extends OsgiCommandSupport {

	private static final Logger LOG = LoggerFactory.getLogger(CreateAssetTopologyCommand.class);

	private final AssetGraphMLProvider assetGraphMLProvider;

	public CreateAssetTopologyCommand(AssetGraphMLProvider assetGraphMLProvider) {
		this.assetGraphMLProvider = assetGraphMLProvider;
	}

	@Option(name = "-i", aliases =  "--providerId", description = "Unique providerId of asset topology", required = false, multiValued = false)
	String providerId;

	@Option(name = "-a", aliases = "--assetLayers", description = "Comma seperated list of asset layers", required = false, multiValued = false)
	String assetLayers;

	@Option(name = "-f", aliases =  "--filter", description = "Optional node filter", required = false, multiValued = false)
	String filter;

	@Option(name = "-l", aliases = "--label", description = "Asset Topology label (shows in topology menu)", required = false, multiValued = false)
	String label;

	@Option(name = "-b", aliases ="--breadcrumbStrategy", description = "Bread Crumb Strategy", required = false, multiValued = false)
	String breadcrumbStrategy;

	@Option(name = "-p", aliases = "--preferredLayout", description = "Preferred Layout", required = false, multiValued = false)
	String preferredLayout;

	@Override
	protected Object doExecute() throws Exception {
		try{
			final GeneratorConfig config = new GeneratorConfigBuilder()
					.withProviderId(providerId)
					.withHierarchy(assetLayers)
					.withLabel(label)
					.withBreadcrumbStrategy(breadcrumbStrategy)
					.withPreferredLayout(preferredLayout)
					.withFilters(filter)
					.build();

			System.out.println("Creating Asset Topology from configuration " + config);
			assetGraphMLProvider.createAssetTopology(config);
			System.out.println("Asset Topology created");
		} catch (Exception e) {
			System.out.println("Error Creating Asset Topology. Exception="+e);
			LOG.error("Error Creating Asset Topology",e);
		}
		return null;
	}
}

