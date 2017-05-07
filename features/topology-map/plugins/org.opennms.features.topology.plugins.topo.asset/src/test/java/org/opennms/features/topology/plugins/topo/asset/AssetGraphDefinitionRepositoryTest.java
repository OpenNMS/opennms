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

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;
import org.opennms.features.topology.plugins.topo.asset.AssetGraphDefinitionRepositoryImpl;
import org.opennms.features.topology.plugins.topo.asset.filter.GeneratorConfigURITest;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetGraphDefinitionRepositoryTest {
	private static final Logger LOG = LoggerFactory.getLogger(GeneratorConfigURITest.class);

	private final String persistentId= "org.opennms.features.topology.plugins.topo.asset";
	private final String uriconfig1="providerId=asset1"
			+ "&label=asset1"
			+ "&assetLayers=asset-country,asset-city,asset-building"
			+ "&filter=asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,testForeignSource2"
			+ "&preferredLayout=Grid Layout"
			+ "&breadcrumbStrategy=SHORTEST_PATH_TO_ROOT";
	private final String uriconfig2="providerId=asset2"
			+ "&label=asset2"
			+ "&assetLayers=asset-country,asset-city,asset-building"
			+ "&filter=asset-displaycategory=!testDisplayCategory;node-foreignsource=testForeignSource1,testForeignSource2"
			+ "&preferredLayout=Grid Layout"
			+ "&breadcrumbStrategy=SHORTEST_PATH_TO_ROOT";


	@Test
	public void test() {
		LOG.debug("start of AssetGraphDefinitionRepositoryTest");
		AssetGraphDefinitionRepositoryImpl assetGraphDefinitionRepository = new AssetGraphDefinitionRepositoryImpl();

		ConfigurationAdmin configurationAdmin= new MockConfigurationAdmin();
		assetGraphDefinitionRepository.setConfigurationAdmin(configurationAdmin);
		assetGraphDefinitionRepository.setPersistentId(persistentId);

		GeneratorConfig config1 = new GeneratorConfigBuilder()
		.withGraphDefinitionUri(uriconfig1)
		.build();
		LOG.debug("config1: "+config1);

		GeneratorConfig config2 = new GeneratorConfigBuilder()
		.withGraphDefinitionUri(uriconfig2)
		.build();
		LOG.debug("config2: "+config2);

		assetGraphDefinitionRepository.addConfigDefinition(config1);

		// adding duplicate throws exception
		boolean expectedException=false;
		try {
			assetGraphDefinitionRepository.addConfigDefinition(config1);
		} catch ( Exception e){
			expectedException=true;
			LOG.debug("    expected Exception thrown="+e.getMessage());
		}
		assertEquals(true,expectedException);

		assertTrue(assetGraphDefinitionRepository.exists(config1.getProviderId()));

		assertFalse(assetGraphDefinitionRepository.exists(config2.getProviderId()));

		assetGraphDefinitionRepository.addConfigDefinition(config2);

		Map<String, GeneratorConfig> configDefinitions = assetGraphDefinitionRepository.getAllConfigDefinitions();

		assertEquals(2,configDefinitions.size());

		StringBuffer msg = new StringBuffer("List of installed asset topology definitions");
		for(String providerId:configDefinitions.keySet()){
			LOG.debug("help");
			GeneratorConfig generatorConfig = configDefinitions.get(providerId);
			String graphDefinitionUriString = GeneratorConfigBuilder.toGraphDefinitionUriString(generatorConfig);
			msg.append("\n providerId:"+providerId);
			msg.append("\n     generatorConfig:"+generatorConfig.toString());
			msg.append("\n     graphDefinitionUriString:"+graphDefinitionUriString);
		}
		LOG.debug(msg.toString());
		LOG.debug("end of AssetGraphDefinitionRepositoryTest");
	}

}
