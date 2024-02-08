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
package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.snmp4j.security.SecurityLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This bean manually overwrites the current {@link TrapdConfig} to use for tests.
 *
 * This should be done by simply overwrite the desired bean, but that cannot be done due to an issue in Spring,
 * but was fixed in Spring 4.2. See https://jira.spring.io/browse/SPR-9567 for more details.
 * As soon as we update to Spring 4.2. this should be migrated to a @Configuration and the init()
 * method should be migrated to a @Bean("trapdConfig") instead.
 */
public class TrapdConfigConfigUpdater {

    @Autowired
    private TrapdConfig m_config;

    @Autowired
    private Trapd trapd;

    // Hacky way of overwriting configuration settings for test execution.
    @PostConstruct
    public void init() throws IOException {
        TrapdConfigBean config = new TrapdConfigBean(m_config);
        config.setSnmpTrapPort(10000); // default 162 is only available as root
        config.setNewSuspectOnTrap(true); // default is false
        config.setBatchSize(1);
        config.setQueueSize(1);
        config.setBatchIntervalMs(0);

        // Include an SNMPv3 users for all security levels
        final List<SnmpV3User> v3users = new ArrayList<>();
        for (SecurityLevel securityLevel : SecurityLevel.values()) {
            Snmpv3User user = new Snmpv3User();
            user.setSecurityName("${scv:auth-" + securityLevel + ":username}"); // Include the security level to make this unique
            user.setSecurityLevel(securityLevel.getSnmpValue());
            user.setAuthProtocol("MD5");
            user.setAuthPassphrase("${scv:auth-" + securityLevel + ":password}");
            user.setEngineId("some-engine-id-" + securityLevel);
            user.setPrivacyPassphrase("0p3nNMSv3");
            user.setPrivacyProtocol("DES");
            v3users.add(TrapdConfigBean.toSnmpV3User(user));
        }
        config.setSnmpV3Users(v3users);

        m_config.update(config);
        
    }

}
