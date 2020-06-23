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