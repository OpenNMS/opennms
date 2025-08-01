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
package org.opennms.features.topology.plugins.topo.asset.cmd;


import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepository;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigList;

@Command(scope = "opennms", name = "asset-topo-list", description="Lists all of the asset topologies currently installed")
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
