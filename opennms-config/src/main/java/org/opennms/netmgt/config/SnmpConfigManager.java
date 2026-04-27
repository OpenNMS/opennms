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

import com.google.common.base.Strings;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.config.snmp.SnmpProfiles;

/**
 * This class handles merging a new Definition into the current running SNMP
 * Configuration.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
public class SnmpConfigManager {

    private static final String DEFAULT_LOCATION = "Default";
    private final SnmpConfig m_config;
	private final List<MergeableDefinition> m_definitions = new ArrayList<>();

	public SnmpConfigManager() {
		m_config = new SnmpConfig();
	}

	/**
	 * <p>
	 * Constructor for SnmpConfigManager.
	 * </p>
	 *
	 * @param config
	 *            a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
	 */
	public SnmpConfigManager(SnmpConfig config) {
		m_config = config != null ? config : new SnmpConfig();

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

	private void setDefinitions(List<MergeableDefinition> definitions) {
		m_definitions.clear();
		m_definitions.addAll(definitions);
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
        removeDefinitionsThatDoNotMatchLocation(eventDef);
		purgeRangesFromDefinitions(eventToMerge);

		if (eventToMerge.isTrivial()) {
			return;
		}

		// add pass
		final boolean isIpMatchOnly = hasIpMatchOnly(eventToMerge.getConfigDef());
		MergeableDefinition matchingDef = findMatchingDefinition(eventToMerge);

		if (matchingDef == null) {
			// If we did not find a matching definition, then we can just add the new definition to the config
			addDefinition(eventToMerge);
		} else if (isIpMatchOnly) {
			// if the definition has only ipMatch values, then we want to replace any existing definition
			// that has the same ipMatch values with the new definition since the new definition could have
			// different SNMP parameters even if the ipMatch values are the same.
			removeIpMatchOnlyDefinition(eventToMerge.getConfigDef());
			addDefinition(eventToMerge);
		} else {
			// If we found a definition with matching SNMP parameters/attributes,
			// then we want to merge the IP specific/range values from the new definition into that existing definition
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

		removeDefinitionsThatDoNotMatchLocation(definition);

		// Find a matching definition.
		// If it is a definition with only ipMatch values, then remove the whole definition if the ipMatch values match.
		// Otherwise, only remove the matching ranges/specifics from the existing definition,
		// then remove any definitions that are left with no ranges/specifics.
		final boolean isIpMatchOnly = hasIpMatchOnly(removableDefinition.getConfigDef());
		MergeableDefinition matchingDef = findMatchingDefinition(removableDefinition);

		if (matchingDef != null) {
			if (isIpMatchOnly) {
				removeIpMatchOnlyDefinition(removableDefinition.getConfigDef());
				return true;
			} else {
				matchingDef.removeRanges(removableDefinition);
				removeEmptyDefinitions();
				return true;
			}
		}

		return false;
	}

	/**
	 * Merge an SnmpProfile into the current configuration.
	 * If the profile has the same label as an existing profile, replace it.
	 * Otherwise add it as a new profile.
	 * @param profile a {@link org.opennms.netmgt.config.snmp.SnmpProfile} object.
	 */
	public void mergeProfileIntoConfig(final SnmpProfile profile) throws IllegalArgumentException {
		if (profile == null || Strings.isNullOrEmpty(profile.getLabel())) {
			throw new IllegalArgumentException("profile must not be null and must have a label");
		}

		final SnmpProfiles snmpProfiles = Objects.requireNonNullElseGet(getConfig().getSnmpProfiles(), SnmpProfiles::new);
		final List<SnmpProfile> existingProfileList =
			Objects.requireNonNullElseGet(snmpProfiles.getSnmpProfiles(), ArrayList::new);

		ArrayList<SnmpProfile> updatedProfiles = new ArrayList<>();

		// find existing profile with same label, if any
		final SnmpProfile existingProfile = findExistingProfile(profile.getLabel());

		if (existingProfile != null) {
			// profile exists, replace it
			updatedProfiles =
				new ArrayList<>(
					existingProfileList.stream()
					.map(p -> p.getLabel().equals(profile.getLabel()) ? profile : p)
					.toList());
		} else {
			// add a new profile
			updatedProfiles = new ArrayList<>(existingProfileList);
			updatedProfiles.add(profile);
		}

		snmpProfiles.setSnmpProfiles(updatedProfiles);
		getConfig().setSnmpProfiles(snmpProfiles);
	}

	/**
	 * Remove profile with the given label from the config.
	 * @param label label of the profile to remove.
	 * @return true when profile is removed else false.
	 */
	public boolean removeProfile(final String label) {
		if (Strings.isNullOrEmpty(label)) {
			throw new IllegalArgumentException("label must exist");
		}

		final SnmpProfiles snmpProfiles = getConfig().getSnmpProfiles();
		if (snmpProfiles == null) {
			return false;
		}
		final List<SnmpProfile> existingProfileList = snmpProfiles.getSnmpProfiles();

		// find existing profile with same label
		final SnmpProfile existingProfile = findExistingProfile(label);

		if (existingProfile != null) {
			ArrayList<SnmpProfile> updatedProfiles =
				new ArrayList<>(
				    existingProfileList.stream()
					.filter(p -> !p.getLabel().equals(label))
					.toList());

			snmpProfiles.setSnmpProfiles(updatedProfiles);
			getConfig().setSnmpProfiles(snmpProfiles);

			return true;
		}

		// most likely could not find profile with given label
		return false;
	}

	private SnmpProfile findExistingProfile(final String label) {
		final SnmpProfiles snmpProfiles = getConfig().getSnmpProfiles();
		if (snmpProfiles == null) {
			return null;
		}

		final List<SnmpProfile> existingProfileList = snmpProfiles.getSnmpProfiles();

		return existingProfileList.stream()
			.filter(p -> p.getLabel().equals(label))
			.findFirst()
			.orElse(null);
	}

    private void removeDefinitionsThatDoNotMatchLocation(Definition eventToDef) {
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
	 * @param eventDefinition
	 */
	private void purgeRangesFromDefinitions(MergeableDefinition eventDefinition) {
		for (MergeableDefinition def : getDefinitions()) {
			def.removeRanges(eventDefinition);
		}

		removeEmptyDefinitions();
	}

	private MergeableDefinition findMatchingDefinition(MergeableDefinition def) {
		// special case: if the definition has only ipMatch,
		// then we try to find matching definition that also has only ipMatch and is an exact match.
		if (hasIpMatchOnly(def.getConfigDef())) {
			final String ipMatchToFind = getIpMatchCompareString(def.getConfigDef());

			for (MergeableDefinition d : getDefinitions()) {
				if (hasIpMatchOnly(d.getConfigDef())) {
					final String ipMatch = getIpMatchCompareString(d.getConfigDef());

					if (ipMatch.equals(ipMatchToFind)) {
						return d;
					}
				}
			}

			return null;
		}

		for (MergeableDefinition d : getDefinitions()) {
			if (d.matches(def)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Removes an existing definition that has only ipMatch values and those values exactly match the given definition's ipMatch values.
	 * Also matches by location if the definition has a location set.
	 */
	private void removeIpMatchOnlyDefinition(Definition definition) {
		if (!hasIpMatchOnly(definition)) {
			return;
		}

		final String ipMatchToFind = getIpMatchCompareString(definition);
		final String locationToMatch = definition.getLocation();

		// remove matching definition from config
		final List<Definition> filteredDefs = getConfig().getDefinitions().stream()
				.filter(d -> !isIpOnlyMatchWithLocation(d, ipMatchToFind, locationToMatch))
				.toList();
		getConfig().setDefinitions(filteredDefs);

		// remove matching definition from (mergeable) definitions
		final List<MergeableDefinition> filteredMergeableDefs = getDefinitions().stream()
				.filter(d -> !isIpOnlyMatchWithLocation(d.getConfigDef(), ipMatchToFind, locationToMatch))
				.toList();
		setDefinitions(filteredMergeableDefs);
	}

	/**
	 * Returns true if the given definition has only ipMatch values, those values match the given ipMatchToFind string,
	 * and the location matches (considering "Default" and null as equivalent).
	 * ipMatchToFind should have been generated using getIpMatchCompareString.
	 */
	private boolean isIpOnlyMatchWithLocation(Definition definition, String ipMatchToFind, String locationToMatch) {
		if (!hasIpMatchOnly(definition) || !getIpMatchCompareString(definition).equals(ipMatchToFind)) {
			return false;
		}

		// Match location, treating "Default" and null as equivalent
		String defLocation = definition.getLocation();
		if (DEFAULT_LOCATION.equals(defLocation)) {
			defLocation = null;
		}
		String matchLocation = locationToMatch;
		if (DEFAULT_LOCATION.equals(matchLocation)) {
			matchLocation = null;
		}

		return Objects.equals(defLocation, matchLocation);
	}

	/**
	 * Returns true if the given definition has only ipMatch values and those values match the given ipMatchToFind string.
	 * ipMatchToFind should have been generated using getIpMatchCompareString.
	 */
	public boolean isIpOnlyMatch(Definition definition, String ipMatchToFind) {
		return hasIpMatchOnly(definition) && getIpMatchCompareString(definition).equals(ipMatchToFind);
	}

	/**
	 * Returns true if the given definition has only ipMatch values and no ranges or specifics.
	 */
	public boolean hasIpMatchOnly(Definition definition) {
		return definition.getIpMatches() != null && !definition.getIpMatches().isEmpty()
			&& definition.getRanges().isEmpty()
			&& definition.getSpecifics().isEmpty();
	}

	/**
	 * Returns a string that can be used to compare the ipMatch values of two definitions.
	 */
	private static String getIpMatchCompareString(Definition def) {
		if (def.getIpMatches() == null || def.getIpMatches().isEmpty()) {
			return "";
		}

		return String.join(",", def.getIpMatches().stream()
			.map(String::toLowerCase).sorted().toList());
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
