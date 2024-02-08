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

import org.opennms.features.scv.api.SecureCredentialsVault;
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

    private SecureCredentialsVault m_scv;

    public SnmpDetectorFactory() {
        super(SnmpDetector.class);
    }

    public void setSecureCredentialsVault(SecureCredentialsVault scv) {
        this.m_scv = scv;
    }


    @SuppressWarnings("unchecked")
    @Override
    public SnmpDetector createDetector(Map<String, String> properties) {
        SnmpDetector snmpDetector = super.createDetector(properties);
        snmpDetector.setSnmpDetectorExecutor(snmpDetectorExecutor);
        snmpDetector.setSecureCredentialsVault(m_scv);
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
            final var map = agentConfigFactory.getAgentConfig(address, location).toMap();
            map.put("location", location);
            return map;
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
            agentConfigMap.put("location", location);
            return agentConfigMap;
        }
    }

    @PreDestroy
    public void destroy() {
        snmpDetectorExecutor.shutdown();
    }
}
