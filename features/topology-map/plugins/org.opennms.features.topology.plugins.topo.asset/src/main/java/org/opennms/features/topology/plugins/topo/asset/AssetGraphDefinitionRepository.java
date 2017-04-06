package org.opennms.features.topology.plugins.topo.asset;

import java.util.Map;

public interface AssetGraphDefinitionRepository {

	/**
	 * Returns Asset topology definition for given providerId
	 * @param providerId
	 * @return GeneratorConfig containing Asset topology definition or null if config does not exist
	 */
	public abstract GeneratorConfig getConfigDefinition(String providerId);

	/**
	 * Returns a map of all asset topology definitions or empty map if no definitions exist
	 * @return map of all GeneratorConfigs indexed by providerId
	 */
	public abstract Map<String, GeneratorConfig> getAllConfigDefinitions();

	/**
	 * removes the config definition for a given providerId. 
	 * Does nothing if this config does not exist.
	 * @param providerId
	 */
	public abstract void removeConfigDefinition(String providerId);

	/**
	 * adds a new config definition to the repository
	 * throws an exception if a configuration with the same providerId already exists
	 * @param generatorConfig
	 */
	public abstract void addConfigDefinition(GeneratorConfig generatorConfig);

	/**
	 * checks if config for providerId exists in the repository. 
	 * @param providerId
	 * @return true if config exists
	 */
	boolean exists(String providerId);

}