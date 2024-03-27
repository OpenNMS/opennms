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
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphMLProvider;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;

@Command(scope = "opennms", name = "asset-topo-remove", description="Removes Asset Topology. Uses default config if options not supplied")
@Service
public class RemoveAssetTopologyCommand implements Action {

	@Reference
	public AssetGraphMLProvider assetGraphMLProvider;

	@Argument(index = 0, name = "providerId", description = "Unique providerId of asset topology (optional)", required = false, multiValued = false)
	String providerId = new GeneratorConfig().getProviderId();

	@Override
	public Object execute() {
		if( providerId == null || providerId.trim().isEmpty()){
			providerId = new GeneratorConfig().getProviderId();
		}
		System.out.println("Removing Asset Topology for providerId=" + providerId);
		assetGraphMLProvider.removeAssetTopology(providerId);
		System.out.println("Removed Asset Topology for providerId=" + providerId);
		return null;
	}

}
