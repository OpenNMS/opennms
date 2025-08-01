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
    public T createDetector(Map<String, String> properties) {
        return (T) super.createDetector(properties);
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        return new DetectRequestImpl(address, port, getRuntimeAttributes(location, address));
    }

    public Map<String, String> getRuntimeAttributes(String location, InetAddress address) {
        if (m_agentConfigFactory == null) {
            throw new IllegalStateException("Cannot determine agent configuration without a SnmpAgentConfigFactory.");
        }
        final var map = m_agentConfigFactory.getAgentConfig(address, location).toMap();
        // Need to embed location into request so minion knows
        map.put("location", location);
        return map;
    }

    public void setAgentConfigFactory(SnmpAgentConfigFactory agentConfigFactory) {
        m_agentConfigFactory = agentConfigFactory;
    }

    public SnmpAgentConfigFactory getAgentConfigFactory() {
        return m_agentConfigFactory;
    }
}
