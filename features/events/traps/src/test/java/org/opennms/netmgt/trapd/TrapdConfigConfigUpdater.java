/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    // Hacky way of overwriting configuration settings for test execution.
    @PostConstruct
    public void init() {
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
            user.setSecurityName("some-security-name-" + securityLevel); // Include the security level to make this unique
            user.setSecurityLevel(securityLevel.getSnmpValue());
            user.setAuthProtocol("MD5");
            user.setAuthPassphrase("0p3nNMSv3");
            user.setEngineId("some-engine-id-" + securityLevel);
            user.setPrivacyPassphrase("0p3nNMSv3");
            user.setPrivacyProtocol("DES");
            v3users.add(TrapdConfigBean.toSnmpV3User(user));
        }
        config.setSnmpV3Users(v3users);

        m_config.update(config);
    }
}
