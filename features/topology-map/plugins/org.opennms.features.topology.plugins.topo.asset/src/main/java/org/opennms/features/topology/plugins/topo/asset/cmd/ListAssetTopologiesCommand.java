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
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigList;

@Command(scope = "asset-topology", name = "list", description="Lists all of the asset topologies currently installed")
@Service
public class ListAssetTopologiesCommand implements Action {

	@Reference
	public AssetGraphDefinitionRepository assetGraphDefinitionRepository;

	@Override
	public Object execute() {
		GeneratorConfigList configDefinitions = assetGraphDefinitionRepository.getAllConfigDefinitions();

		System.out.println("List of installed asset topology definitions:");
		System.out.print(JaxbUtils.marshal(configDefinitions));
		return null;
	}

}
