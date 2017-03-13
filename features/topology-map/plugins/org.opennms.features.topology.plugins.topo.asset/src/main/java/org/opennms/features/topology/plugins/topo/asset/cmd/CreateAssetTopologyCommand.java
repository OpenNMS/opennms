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
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphMLProvider;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "asset-topology/create", name = "createAssetTopology", description="Creates Asset Topology. Uses default config if options not supplied")
public class CreateAssetTopologyCommand extends OsgiCommandSupport {
	private static final Logger LOG = LoggerFactory.getLogger(CreateAssetTopologyCommand.class);

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

	@Option(name = "-i", aliases =  "--providerId", description = "Unique providerId of asset topology", required = false, multiValued = false)
	String providerId = null;

	@Option(name = "-a", aliases = "--assetLayers", description = "Comma seperated list of asset layers", required = false, multiValued = false)
	String assetLayers = null;

	@Option(name = "-f", aliases =  "--filter", description = "Optional node filter", required = false, multiValued = false)
	String filter = null;

	@Option(name = "-u", aliases = "--unallocatedGraph", description = "Generate Unallocated Nodes Graph", required = false, multiValued = false)
	String generateUnallocatedStr = null;

	@Option(name = "-l", aliases = "--label", description = "Asset Topology label (shows in topology menu)", required = false, multiValued = false)
	String label = null;

	@Option(name = "-b", aliases ="--breadcrumbStrategy", description = "Bread Crumb Strategy", required = false, multiValued = false)
	String breadcrumbStrategy = null;

	@Option(name = "-p", aliases = "--preferredLayout", description = "Preferred Layout", required = false, multiValued = false)
	String preferredLayout = null;

	@Override
	protected Object doExecute() throws Exception {
		try{
			GeneratorConfig config = new GeneratorConfig();
			
			if (providerId==null) providerId=defaultGeneratorConfig.getProviderId();
			config.setProviderId(providerId);
			
			if(assetLayers==null) assetLayers=defaultGeneratorConfig.getAssetLayers();
			config.setAssetLayers(assetLayers);
			
			if(filter==null) filter = defaultGeneratorConfig.getFilter();
			config.setFilter(filter);
			
			if (generateUnallocatedStr==null) generateUnallocatedStr = (Boolean.toString(defaultGeneratorConfig.getGenerateUnallocated()));
			config.setGenerateUnallocated(Boolean.valueOf(generateUnallocatedStr));
			
			if(label==null) label =defaultGeneratorConfig.getLabel();
			config.setLabel(label);
			
			if (breadcrumbStrategy==null) breadcrumbStrategy=defaultGeneratorConfig.getBreadcrumbStrategy();
			config.setBreadcrumbStrategy(breadcrumbStrategy);
			
			if (preferredLayout==null) preferredLayout=defaultGeneratorConfig.getPreferredLayout();
			config.setPreferredLayout(preferredLayout);

			System.out.println("Creating Asset Topology from configuration "+config);

			assetGraphMLProvider.createAssetTopology(config);

			System.out.println("Asset Topology created");

		} catch (Exception e) {
			System.out.println("Error Creating Asset Topology. Exception="+e);
			LOG.error("Error Creating Asset Topology",e);
		}
		return null;
	}


}

