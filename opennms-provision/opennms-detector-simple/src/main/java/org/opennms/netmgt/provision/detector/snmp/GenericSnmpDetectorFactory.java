/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.DetectRequestImpl;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericSnmpDetectorFactory<T extends SnmpDetector> extends GenericServiceDetectorFactory<SnmpDetector> {

    @Autowired(required=false)
    private SnmpAgentConfigFactory m_agentConfigFactory;

    @SuppressWarnings("unchecked")
    public GenericSnmpDetectorFactory(Class<T> clazz) {
        super((Class<SnmpDetector>) clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createDetector() {
        return (T)super.createDetector();
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        return new DetectRequestImpl(address, port, getRuntimeAttributes(location, address));
    }

    public Map<String, String> getRuntimeAttributes(String location, InetAddress address) {
        if (m_agentConfigFactory == null) {
            throw new IllegalStateException("Cannot determine agent configuration without a SnmpAgentConfigFactory.");
        }
        return m_agentConfigFactory.getAgentConfig(address, location).toMap();
    }

    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }
}
