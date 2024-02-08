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
