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

public interface AssetGraphDefinitionRepository {

	/**
	 * Returns Asset topology definition for given providerId
	 * @param providerId
	 * @return GeneratorConfig containing Asset topology definition or null if config does not exist
	 */
	GeneratorConfig getConfigDefinition(String providerId);

	/**
	 * Returns a map of all asset topology definitions or empty map if no definitions exist
	 * @return map of all GeneratorConfigs indexed by providerId
	 */
	GeneratorConfigList getAllConfigDefinitions();

	/**
	 * removes the config definition for a given providerId. 
	 * Does nothing if this config does not exist.
	 * @param providerId
	 */
	void removeConfigDefinition(String providerId);

	/**
	 * adds a new config definition to the repository
	 * throws an exception if a configuration with the same providerId already exists
	 * @param generatorConfig
	 */
	void addConfigDefinition(GeneratorConfig generatorConfig);

	/**
	 * checks if config for providerId exists in the repository. 
	 * @param providerId
	 * @return true if config exists
	 */
	boolean exists(String providerId);

}