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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "asset-topology", name = "createNodeInfo", description="Creates Debug Node Info File. Uses default config if options are not supplied.")
public class CreateNodeInfoCommand extends OsgiCommandSupport {
	private static final Logger LOG = LoggerFactory.getLogger(CreateNodeInfoCommand .class);

	private final AssetGraphMLProvider assetGraphMLProvider;

	public CreateNodeInfoCommand(AssetGraphMLProvider assetGraphMLProvider) {
		this.assetGraphMLProvider = assetGraphMLProvider;
	}
	@Option(name = "-f", aliases =  "--filter", description = "Optional node filter", required = false, multiValued = false)
	String filter = new GeneratorConfig().getFilter();

	@Override
	protected Object doExecute() throws Exception {
		try{
			// TODO MVR implement ...
//			GeneratorConfig config = new GeneratorConfig();
//
//			if(filter==null) filter=defaultGeneratorConfig.getFilter();
//
//			config.setFilter(filter);
//
//			System.out.println("Creating Node Info File from filter: "+filter);
//
//			assetGraphMLProvider.createNodeInfoFile(config);
//
//			System.out.println("Node Info File created");

		} catch (Exception e) {
			System.out.println("Error Creating Node Info File. Exception="+e);
			LOG.error("Error Creating Node Info File. ",e);
		}
		return null;
	}


}


