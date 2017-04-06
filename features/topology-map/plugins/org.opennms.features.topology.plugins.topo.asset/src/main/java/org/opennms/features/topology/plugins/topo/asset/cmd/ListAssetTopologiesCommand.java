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


import java.util.List;
import java.util.Map;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "asset-topology", name = "list", description="Lists all of the asset topologies currently installed")
public class ListAssetTopologiesCommand extends OsgiCommandSupport {
	private static final Logger LOG = LoggerFactory.getLogger(ListAssetTopologiesCommand.class);

	private final AssetGraphDefinitionRepository assetGraphDefinitionRepository;

	public ListAssetTopologiesCommand(AssetGraphDefinitionRepository assetGraphDefinitionRepository) {
		this.assetGraphDefinitionRepository = assetGraphDefinitionRepository;
	}

	@Argument(name = "detail", description = "if 'all' command will display all fields including --uriParams string", required = false, multiValued = false)
	String detail="";

	@Override
	protected Object doExecute() throws Exception {
		try{
			StringBuffer msg = new StringBuffer("List of installed asset topology definitions");
			Map<String, GeneratorConfig> configDefinitions = assetGraphDefinitionRepository.getAllConfigDefinitions();

			for(String providerId:configDefinitions.keySet()){
				GeneratorConfig generatorConfig = configDefinitions.get(providerId);
				String graphDefinitionUriString = GeneratorConfigBuilder.toGraphDefinitionUriString(generatorConfig);
				msg.append("\n --providerId:"+providerId);
				msg.append("\n     --label:"+generatorConfig.getLabel());
				
				msg.append("\n     --assetLayers:");
				List<String> l = generatorConfig.getLayerHierarchies();
				for(int i=0; i<l.size(); i++){
					msg.append(l.get(i));
					if(i<l.size()) msg.append(",");
				}
				msg.append("\n     --filter:");
				List<String> f = generatorConfig.getFilters();
				for(int i=0; i<f.size(); i++){
					msg.append(f.get(i));
					if(i<f.size()) msg.append(";");
				}
				
				msg.append("\n     --preferredLayout:"+generatorConfig.getPreferredLayout());
				msg.append("\n     --breadcrumbStrategy:"+generatorConfig.getBreadcrumbStrategy());
				if("all".equals(detail.trim())){
					msg.append("\n     --uriParams:"+graphDefinitionUriString);
				}
			}
			System.out.println(msg.toString());
			LOG.debug(msg.toString());

		} catch (Exception e) {
			LOG.error("Error listing installed asset topology definitions Exception=",e);
			System.out.println("Error listing installed asset topology definitions Exception="+e);
		}
		return null;
	}
}

