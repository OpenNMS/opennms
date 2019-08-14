/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.snmp.profile.mapper.impl;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

public class SnmpProfileMapperImpl implements SnmpProfileMapper {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpProfileMapperImpl.class);

    @Autowired
    private SnmpAgentConfigFactory agentConfigFactory;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private LocationAwareSnmpClient locationAwareSnmpClient;

    public SnmpProfileMapperImpl(FilterDao filterDao, SnmpAgentConfigFactory agentConfigFactory, LocationAwareSnmpClient locationAwareSnmpClient) {
        this.agentConfigFactory = Objects.requireNonNull(agentConfigFactory);
        this.filterDao = Objects.requireNonNull(filterDao);
        this.locationAwareSnmpClient = Objects.requireNonNull(locationAwareSnmpClient);
    }

    public SnmpProfileMapperImpl() {
    }

    private static final String SYS_OBJECTID_INSTANCE = ".1.3.6.1.2.1.1.2.0";

    @Override
    public Optional<SnmpAgentConfig> getAgentConfigFromProfiles(InetAddress inetAddress, String location, String oid) {

        List<SnmpProfile> snmpProfiles = agentConfigFactory.getProfiles();
        // Get matching profiles for this IpAddress.
        List<SnmpProfile> matchedProfiles = snmpProfiles.stream()
                .filter(snmpProfile -> isFilterExpressionValid(inetAddress, snmpProfile.getFilterExpression()))
                .collect(Collectors.toList());

        for (SnmpProfile snmpProfile : matchedProfiles) {

            Optional<SnmpAgentConfig> config = fitProfile(snmpProfile, inetAddress, location, oid);
            if (config.isPresent()) {
                return config;
            }
        }
        return Optional.empty();
    }

    private Optional<SnmpAgentConfig> fitProfile(SnmpProfile snmpProfile, InetAddress inetAddress, String location, String oid) {
        //Get agent config from profile.
        SnmpAgentConfig agentConfig = agentConfigFactory.getAgentConfigFromProfile(snmpProfile, inetAddress);
        try {
            if (Strings.isNullOrEmpty(oid)) {
                oid = SYS_OBJECTID_INSTANCE;
            }
            if (Strings.isNullOrEmpty(location)) {
                location = "Default";
            }
            SnmpValue snmpValue = locationAwareSnmpClient.get(agentConfig, oid)
                    .withLocation(location)
                    .withDescription("Snmp-Profile:" + snmpProfile.getLabel())
                    .execute()
                    .get();
            if (snmpValue != null && !snmpValue.isError()) {
                return Optional.of(agentConfig);
            }
        } catch (Exception e) {
            LOG.warn("Exception while trying to get sysObjectID for the profile {}", snmpProfile.getLabel(), e);
        }
        return Optional.empty();
    }


    @Override
    public Optional<SnmpAgentConfig> getAgentConfigFromProfiles(InetAddress inetAddress, String location) {
        return getAgentConfigFromProfiles(inetAddress, location, SYS_OBJECTID_INSTANCE);
    }

    @Override
    public Optional<SnmpAgentConfig> fitProfile(String profileLabel, InetAddress inetAddress, String location, String oid) {

        Optional<SnmpAgentConfig> agentConfig = Optional.empty();
        if (Strings.isNullOrEmpty(profileLabel)) {
            agentConfig = getAgentConfigFromProfiles(inetAddress, location, oid);

        } else {
            List<SnmpProfile> profiles = agentConfigFactory.getProfiles();
            Optional<SnmpProfile> matchingProfile = profiles.stream()
                    .filter(profile -> profile.getLabel().equals(profileLabel))
                    .findFirst();
            if (matchingProfile.isPresent()) {
                agentConfig = fitProfile(matchingProfile.get(), inetAddress, location, oid);
            }
        }
        return agentConfig;
    }

    private boolean isFilterExpressionValid(InetAddress inetAddress, String filterExpression) {
        // Consider profiles without filter expression or an empty one as matching.
        if (Strings.isNullOrEmpty(filterExpression)) {
            return true;
        } else {
            try {
                return filterDao.isValid(inetAddress.getHostAddress(), filterExpression);
            } catch (FilterParseException e) {
                LOG.warn("Filter expression '{}' is invalid. ", e);
            }
        }
        return false;
    }

    public void setLocationAwareSnmpClient(LocationAwareSnmpClient locationAwareSnmpClient) {
        this.locationAwareSnmpClient = locationAwareSnmpClient;
    }

    public SnmpAgentConfigFactory getAgentConfigFactory() {
        return agentConfigFactory;
    }

    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        this.agentConfigFactory = agentConfigFactory;
    }

    public FilterDao getFilterDao() {
        return filterDao;
    }

    public void setFilterDao(FilterDao filterDao) {
        this.filterDao = filterDao;
    }
}
