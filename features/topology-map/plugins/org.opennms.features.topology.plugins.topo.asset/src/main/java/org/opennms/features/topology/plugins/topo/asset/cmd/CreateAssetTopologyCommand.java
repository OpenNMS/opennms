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


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphMLProvider;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;

@Command(scope = "asset-topology/create", name = "createAssetTopology", description="Creates Asset Topology")
public class CreateAssetTopologyCommand extends OsgiCommandSupport {

	private AssetGraphMLProvider assetGraphMLProvider;

	private GeneratorConfig defaultGeneratorConfig;

	public AssetGraphMLProvider getAssetGraphMLProvider() {
		return assetGraphMLProvider;
	}

	public void setAssetGraphMLProvider(AssetGraphMLProvider assetGraphMLProvider) {
		this.assetGraphMLProvider = assetGraphMLProvider;
	}

	public GeneratorConfig getDefaultGeneratorConfig() {
		return defaultGeneratorConfig;
	}

	public void setDefaultGeneratorConfig(GeneratorConfig defaultGeneratorConfig) {
		this.defaultGeneratorConfig = defaultGeneratorConfig;
	}

	@Argument(index = 0, name = "providerId", description = "Unique providerId of asset topology", required = true, multiValued = false)
	String providerId = defaultGeneratorConfig.getProviderId();

	@Argument(index = 1, name = "assetLayers", description = "Comma seperated list of asset layers", required = true, multiValued = false)
	String assetLayers = defaultGeneratorConfig.getAssetLayers();

	@Argument(index = 2, name = "filter", description = "Topology node filter", required = false, multiValued = false)
	String filter = defaultGeneratorConfig.getFilter();

	@Argument(index = 3, name = "generateUnallocated", description = "Generate Unallocated Nodes Graph", required = false, multiValued = false)
	String generateUnallocatedStr = Boolean.toString(defaultGeneratorConfig.getGenerateUnallocated());

	@Argument(index = 4, name = "label", description = "Asset Topology label (shows in topology menu - defaults to providerId )", required = false, multiValued = false)
	String label = defaultGeneratorConfig.getLabel();

	@Argument(index = 5, name = "breadcrumbStrategy", description = "Bread Crumb Strategy", required = false, multiValued = false)
	String breadcrumbStrategy = defaultGeneratorConfig.getBreadcrumbStrategy();

	@Argument(index = 6, name = "preferredLayout", description = "Preferred Layout", required = false, multiValued = false)
	String preferredLayout = defaultGeneratorConfig.getPreferredLayout();

	@Override
	protected Object doExecute() throws Exception {
		try{
			GeneratorConfig config = new GeneratorConfig();
			config.setProviderId(providerId);
			config.setAssetLayers(assetLayers);
			config.setFilter(filter);
			config.setGenerateUnallocated(Boolean.valueOf(generateUnallocatedStr));
			config.setLabel(label);
			config.setBreadcrumbStrategy(breadcrumbStrategy);
			config.setPreferredLayout(preferredLayout);

			System.out.println("Creating Asset Topology from configuration "+config);

			assetGraphMLProvider.createAssetTopology(config);

			System.out.println("Asset Topology created");

		} catch (Exception e) {
			System.out.println("Error Creating Asset Topology. Exception="+e);
		}
		return null;
	}


}

