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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphMLProvider;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfig;
import org.opennms.features.topology.plugins.topo.asset.GeneratorConfigBuilder;

@Command(scope = "opennms", name = "asset-topo-create", description="Creates Asset Topology. Uses default config if options are not supplied.")
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
