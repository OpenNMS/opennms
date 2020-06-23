/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.snmp;

import static org.opennms.netmgt.snmp.SnmpAgentConfig.AGENT_CONFIG_PREFIX;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PreDestroy;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.provision.support.AgentBasedSyncAbstractDetector;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Component
public class SnmpDetectorFactory extends GenericSnmpDetectorFactory<SnmpDetector> {

    private final ThreadFactory snmpDetectorThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("snmp-detector-%d")
            .build();
    private final ExecutorService snmpDetectorExecutor = Executors.newCachedThreadPool(snmpDetectorThreadFactory);

    public SnmpDetectorFactory() {
        super(SnmpDetector.class);
    }


    @SuppressWarnings("unchecked")
    @Override
    public SnmpDetector createDetector(Map<String, String> properties) {
        SnmpDetector snmpDetector = super.createDetector(properties);
        snmpDetector.setSnmpDetectorExecutor(snmpDetectorExecutor);
        return snmpDetector;
    }

    @Override
    public Map<String, String> getRuntimeAttributes(String location, InetAddress address) {
        SnmpAgentConfigFactory agentConfigFactory = getAgentConfigFactory();
        if (agentConfigFactory == null) {
            throw new IllegalStateException("Cannot determine agent configuration without a SnmpAgentConfigFactory.");
        }

        List<SnmpProfile> snmpProfiles = agentConfigFactory.getProfiles();
        if (snmpProfiles.isEmpty()) {
            return agentConfigFactory.getAgentConfig(address, location).toMap();
        } else {
            // SNMP config has profiles, detector should handle multiple agent configs.
            Map<String, String> agentConfigMap = new HashMap<>();
            agentConfigMap.put(AgentBasedSyncAbstractDetector.HAS_MULTIPLE_AGENT_CONFIGS, Boolean.toString(true));
            // Add default config as string with profile label as key.
            String defaultConfig = agentConfigFactory.getAgentConfig(address, location).toProtocolConfigString();
            agentConfigMap.put(AGENT_CONFIG_PREFIX + SnmpAgentConfig.PROFILE_LABEL_FOR_DEFAULT_CONFIG, defaultConfig);
            // Add snmp profile label as key and config converted to string as value.
            snmpProfiles.forEach((snmpProfile -> {
                SnmpAgentConfig snmpAgentConfig = agentConfigFactory.getAgentConfigFromProfile(snmpProfile, address);
                agentConfigMap.put(AGENT_CONFIG_PREFIX + snmpAgentConfig.getProfileLabel(), snmpAgentConfig.toProtocolConfigString());
            }));
            return agentConfigMap;
        }
    }

    @PreDestroy
    public void destroy() {
        snmpDetectorExecutor.shutdown();
    }
}
