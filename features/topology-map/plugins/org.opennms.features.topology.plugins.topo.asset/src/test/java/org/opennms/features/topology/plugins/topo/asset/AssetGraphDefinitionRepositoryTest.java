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
