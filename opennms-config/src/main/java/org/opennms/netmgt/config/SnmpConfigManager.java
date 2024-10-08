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
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;

/**
 * This class handles merging a new Definition into the current running SNMP
 * Configuration.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
public class SnmpConfigManager {

    private static final String DEFAULT_LOCATION = "Default";
    private SnmpConfig m_config;
	private List<MergeableDefinition> m_definitions = new ArrayList<>();

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
		for (Definition def : m_config.getDefinitions()) {
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
		if (areEquals(m_config.getTTL(), def.getTTL())) def.setTTL(null);
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

        removeDefinitionsthatDontMatchLocation(eventDef);
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
	 * Remove definition from the base config.
	 * @param definition a @{@link Definition} object
	 * @return true when definition is removed else false.
	 */
	public boolean removeDefinition(final Definition definition) {
		MergeableDefinition removableDefinition = new MergeableDefinition(definition);

		removeDefinitionsthatDontMatchLocation(definition);
		// Find a matching definition and remove range from that definition
		MergeableDefinition matchingDef = findMatchingDefinition(removableDefinition);
		if (matchingDef != null) {
			matchingDef.removeRanges(removableDefinition);
			removeEmptyDefinitions();
			return true;
		}
		return false;
	}

    private void removeDefinitionsthatDontMatchLocation(Definition eventToDef) {

        for (Iterator<MergeableDefinition> iter = getDefinitions().iterator(); iter.hasNext();) {
            MergeableDefinition def = iter.next();
            String location = def.getConfigDef().getLocation();
            String locationFromEvent = eventToDef.getLocation();
            if (DEFAULT_LOCATION.equals(location)) {
                location = null;
                def.getConfigDef().setLocation(location);
            }
            if (DEFAULT_LOCATION.equals(locationFromEvent)) {
                locationFromEvent = null;
                eventToDef.setLocation(locationFromEvent);
            }
            if (!Objects.equals(location, locationFromEvent)) {
                iter.remove();
            }
        }

    }

    /**
	 * This method purges specifics and ranges from definitions that don't match
	 * the attributes specified in the event (the updateDef)
	 * 
	 *
	 * @param eventDefinition
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
