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
package org.opennms.features.topology.plugins.topo.asset;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetGraphDefinitionRepositoryTest {
	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphDefinitionRepositoryTest.class);

	@Test
	public void test() {
		System.setProperty("opennms.home", "target");
		Paths.get("target", "etc").toFile().mkdirs();
		File configFile = Paths.get("target", "etc", "org.opennms.features.topology.plugins.topo.asset.xml").toFile();

        configFile.delete(); // make sure we start clean

		LOG.debug("start of AssetGraphDefinitionRepositoryTest");
		AssetGraphDefinitionRepositoryImpl assetGraphDefinitionRepository = new AssetGraphDefinitionRepositoryImpl();

		final GeneratorConfig config1 = new GeneratorConfigBuilder()
				.withProviderId("asset1")
				.withLabel("asset1")
				.withHierarchy("asset-country,asset-city,asset-building")
				.withFilters("asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,testForeignSource2")
				.withPreferredLayout("Grid Layout")
				.withBreadcrumbStrategy("SHORTEST_PATH_TO_ROOT")
				.build();
		LOG.debug("config1: "+config1);

		final GeneratorConfig config2 = new GeneratorConfigBuilder()
				.withProviderId("asset2")
				.withLabel("asset2")
				.withHierarchy("asset-country,asset-city,asset-building")
				.withFilters("asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,testForeignSource2")
				.withPreferredLayout("Grid Layout")
				.withBreadcrumbStrategy("SHORTEST_PATH_TO_ROOT")
				.build();
		LOG.debug("config2: "+config2);

		assetGraphDefinitionRepository.addConfigDefinition(config1);

		assertEquals(Boolean.TRUE, assetGraphDefinitionRepository.exists(config1.getProviderId()));

		assertEquals(Boolean. FALSE,  assetGraphDefinitionRepository.exists(config2.getProviderId()));

		assetGraphDefinitionRepository.addConfigDefinition(config2);

		GeneratorConfigList configDefinitions = assetGraphDefinitionRepository.getAllConfigDefinitions();

		assertEquals(2, configDefinitions.size());

		LOG.debug("List of installed asset topology definitions");
		LOG.debug("{}", JaxbUtils.marshal(configDefinitions));
		LOG.debug("End of {}", getClass().getSimpleName());

        configFile.delete(); // let's leave it clean, too
	}

}
