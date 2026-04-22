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
package org.opennms.netmgt.config.mock;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.api.SnmpConfigDao;

public class MockSnmpPeerFactory extends SnmpPeerFactory {
    public MockSnmpPeerFactory() {
        super();
        this.snmpConfigDao = new SnmpConfigDao() {
            private SnmpConfig snmpConfig;

            {{
                snmpConfig = new SnmpConfig();
                snmpConfig.setVersion("v2c");
                snmpConfig.setReadCommunity("public");
                snmpConfig.setWriteCommunity("private");
                snmpConfig.setTimeout(1800);
                snmpConfig.setRetry(1);
            }}

            @Override
            public SnmpConfig getConfig() {
                return snmpConfig;
            }

            @Override
            public void updateConfig(final SnmpConfig snmpConfig) {
                this.snmpConfig = snmpConfig;
            }
        };
    }
}
