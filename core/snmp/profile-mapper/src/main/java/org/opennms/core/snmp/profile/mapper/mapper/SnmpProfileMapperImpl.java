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

package org.opennms.core.snmp.profile.mapper.mapper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;

import com.google.common.base.Strings;

public class SnmpProfileMapperImpl implements SnmpProfileMapper {

    private SnmpAgentConfigFactory agentConfigFactory;

    private FilterDao filterDao;


    @Override
    public List<SnmpAgentConfig> getAgentConfigs(InetAddress inetAddress) {
        List<SnmpProfile> snmpProfiles = agentConfigFactory.getProfiles();
        // Always return profiles that don't have any filter expression defined.
        List<SnmpProfile> matchedProfiles = snmpProfiles.stream().filter(snmpProfile ->
                Strings.isNullOrEmpty(snmpProfile.getFilterExpression())).collect(Collectors.toList());

        for (SnmpProfile snmpProfile : snmpProfiles) {
            if (Strings.isNullOrEmpty(snmpProfile.getFilterExpression())) {
                boolean isRuleValid = filterDao.isRuleMatching(snmpProfile.getFilterExpression());
                if (isRuleValid) {
                    List<InetAddress> addressList = filterDao.getIPAddressList(snmpProfile.getFilterExpression());
                    if (addressList.contains(inetAddress)) {
                        matchedProfiles.add(snmpProfile);
                    }
                }
            }
        }
        List<SnmpAgentConfig> agentConfigList = new ArrayList<>();
        matchedProfiles.forEach(snmpProfile -> {
            SnmpAgentConfig agentConfig = agentConfigFactory.getAgentConfigFromProfile(snmpProfile, inetAddress);
            agentConfigList.add(agentConfig);
        });
        return agentConfigList;
    }

    @Override
    public void updateDefinition(SnmpAgentConfig snmpAgentConfig, String location) {
        agentConfigFactory.saveAgentConfigAsDefinition(snmpAgentConfig, location);
    }

    @Override
    public void deleteFromDefinition(InetAddress inetAddress) {

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
