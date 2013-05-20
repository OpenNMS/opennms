/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;

/**
 * This class handles merging a new Definition into the current running SNMP
 * Configuration.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
public class SnmpConfigManager {

	private SnmpConfig m_config;
	private List<MergeableDefinition> m_definitions = new ArrayList<MergeableDefinition>();

	/**
	 * <p>
	 * Constructor for SnmpConfigManager.
	 * </p>
	 * 
	 * @param config
	 *            a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
	 */
	public SnmpConfigManager(SnmpConfig config) {
		m_config = config;
		for (Definition def : m_config.getDefinitionCollection()) {
			m_definitions.add(new MergeableDefinition(def));
		}
	}

	/**
	 * Removes all values from the definition (def) which are already set by the
	 * default {@link #m_config} object and are not different from those values
	 * (e.g. the port of the definition is set and equally to the port set in
	 * the SnmpConfig)
	 * 
	 * @param def
	 *            The Definition where the defaults get removed
	 */
	private void removeDefaults(Definition def) {
		if (areEquals(m_config.getPort(), def.getPort())) def.setPort(null);
		if (areEquals(m_config.getAuthPassphrase(), def.getAuthPassphrase())) def.setAuthPassphrase(null);
		if (areEquals(m_config.getAuthProtocol(), def.getAuthProtocol())) def.setAuthProtocol(null);
		if (areEquals(m_config.getContextEngineId(), def.getContextEngineId())) def.setContextEngineId(null);
		if (areEquals(m_config.getContextName(), def.getContextName())) def.setContextName(null);
		if (areEquals(m_config.getEngineId(), def.getEngineId())) def.setEngineId(null);
		if (areEquals(m_config.getEnterpriseId(), def.getEnterpriseId())) def.setEnterpriseId(null);
		if (areEquals(m_config.getMaxRepetitions(), def.getMaxRepetitions())) def.setMaxRepetitions(null);
		if (areEquals(m_config.getMaxVarsPerPdu(), def.getMaxVarsPerPdu())) def.setMaxVarsPerPdu(null);
		if (areEquals(m_config.getPrivacyPassphrase(), def.getPrivacyPassphrase())) def.setPrivacyPassphrase(null);
		if (areEquals(m_config.getPrivacyProtocol(), def.getPrivacyProtocol())) def.setPrivacyProtocol(null);
		if (areEquals(m_config.getProxyHost(), def.getProxyHost())) def.setProxyHost(null);
		if (areEquals(m_config.getMaxRequestSize(), def.getMaxRequestSize())) def.setMaxRequestSize(null);
		if (areEquals(m_config.getWriteCommunity(), def.getWriteCommunity())) def.setWriteCommunity(null);
		if (areEquals(m_config.getVersion(), def.getVersion())) def.setVersion(null);
		if (areEquals(m_config.getTimeout(), def.getTimeout())) def.setTimeout(null);
		if (areEquals(m_config.getSecurityName(), def.getSecurityName())) def.setSecurityName(null);
		if (areEquals(m_config.getSecurityLevel(), def.getSecurityLevel())) def.setSecurityLevel(null);
		if (areEquals(m_config.getRetry(), def.getRetry())) def.setRetry(null);
		if (areEquals(m_config.getReadCommunity(), def.getReadCommunity())) def.setReadCommunity(null);
	}

	/**
	 * <p>
	 * getConfig
	 * </p>
	 * 
	 * @return a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
	 */
	public SnmpConfig getConfig() {
		return m_config;
	}

	private List<MergeableDefinition> getDefinitions() {
		return m_definitions;
	}

	private void addDefinition(MergeableDefinition def) {
		m_definitions.add(def);
		getConfig().addDefinition(def.getConfigDef());
	}

	private void removeEmptyDefinitions() {
		for (Iterator<MergeableDefinition> iter = getDefinitions().iterator(); iter.hasNext();) {
			MergeableDefinition def = iter.next();
			if (def.isEmpty()) {
				getConfig().removeDefinition(def.getConfigDef());
				iter.remove();
			}
		}
	}

	/**
	 * This is the exposed method for moving the data from a configureSNMP event
	 * into the SnmpConfig from SnmpPeerFactory.
	 * 
	 * @param eventDef
	 *            a {@link org.opennms.netmgt.config.snmp.Definition} object.
	 */
	public void mergeIntoConfig(final Definition eventDef) {
		removeDefaults(eventDef); 
		MergeableDefinition eventToMerge = new MergeableDefinition(eventDef);

		// remove pass
		purgeRangesFromDefinitions(eventToMerge);

		if (eventToMerge.isTrivial()) return;

		// add pass
		MergeableDefinition matchingDef = findMatchingDefinition(eventToMerge);
		if (matchingDef == null) {
			addDefinition(eventToMerge);
		} else {
			matchingDef.mergeMatchingAttributeDef(eventToMerge);
		}

	}

	/**
	 * This method purges specifics and ranges from definitions that don't match
	 * the attributes specified in the event (the updateDef)
	 * 
	 * @param updatedDef
	 * @param eventDef
	 */
	private void purgeRangesFromDefinitions(MergeableDefinition eventDefinition) {
		for (MergeableDefinition def : getDefinitions()) {
			def.removeRanges(eventDefinition);
		}
		removeEmptyDefinitions();
	}


	private MergeableDefinition findMatchingDefinition(MergeableDefinition def) {
		for (MergeableDefinition d : getDefinitions()) {
			if (d.matches(def)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Checks if the two objects are equal or not. They are equal if 
	 * <ul>
	 * 	<li>obj1 and obj2 are null</li>
	 *  <li>obj1 and obj2 are not null and obj1.equals(obj2)</li>
	 * </ul>
	 * 
	 * Otherwise they are not equal.
	 * 
	 * @param obj1 Object 1
	 * @param obj2 Object 2
	 * @return true if obj1 and obj2 are equal, otherwise false.
	 */
	protected static final <T> boolean areEquals(T obj1, T obj2) {
		boolean match = false;
        if (obj1 == null && obj2 == null) {
            match = true;
        } else if (obj1 == null || obj2 == null) {
            match = false;
        } else if (obj1.equals(obj2)) {
            match = true;
        }
        return match;
	}
}
