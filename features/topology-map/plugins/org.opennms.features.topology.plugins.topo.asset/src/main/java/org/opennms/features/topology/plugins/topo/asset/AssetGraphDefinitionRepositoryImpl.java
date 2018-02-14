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


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists Asset Graph Definitions in a local (xml) configuration file.
 *
 * @author mvrueden
 */
public class AssetGraphDefinitionRepositoryImpl implements AssetGraphDefinitionRepository {

	private static final String FILE_NAME = "org.opennms.features.topology.plugins.topo.asset.xml";
	private static final Logger LOG = LoggerFactory.getLogger(AssetGraphDefinitionRepositoryImpl.class);

	@Override
	public GeneratorConfig getConfigDefinition(String providerId) {
		return readGeneratorConfigList().getConfig(providerId);
	}

	@Override
	public boolean exists(String providerId){
		return getConfigDefinition(providerId) != null;
	}

	@Override
	public GeneratorConfigList getAllConfigDefinitions() {
		return new GeneratorConfigList(readGeneratorConfigList().getConfigs());
	}

	@Override
	public void removeConfigDefinition(String providerId) {
		GeneratorConfigList generatorConfigList = readGeneratorConfigList();
		generatorConfigList.removeConfig(providerId);
		persist(generatorConfigList);
	}

	@Override
	public void addConfigDefinition(GeneratorConfig generatorConfig) {
		GeneratorConfigList generatorConfigList = readGeneratorConfigList();
		generatorConfigList.addConfig(generatorConfig);
		persist(generatorConfigList);
	}

	private GeneratorConfigList readGeneratorConfigList() {
		File configFile = getConfigFile();
		if (configFile.exists()) {
			return JaxbUtils.unmarshal(GeneratorConfigList.class, configFile);
		}
		return new GeneratorConfigList();
	}

	private void persist(GeneratorConfigList generatorConfigList) {
		final File configFile = getConfigFile();
		try {
			JaxbUtils.marshal(generatorConfigList, configFile);
		} catch (final IOException e) {
			LOG.error("Unable to write graph definition to {}", configFile, e);
			throw new IllegalStateException("Failed to write graph definition to " + configFile, e);
		}
	}

	private File getConfigFile() {
		final String parent = Objects.requireNonNull(System.getProperty("opennms.home"));
		final Path configFilePath = Paths.get(parent, "etc", FILE_NAME);
		return configFilePath.toFile();
	}
}
