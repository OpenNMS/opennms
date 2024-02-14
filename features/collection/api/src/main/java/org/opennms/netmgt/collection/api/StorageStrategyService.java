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
package org.opennms.netmgt.collection.api;

import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * <p>StorageStrategyService interface.</p>
 */
public interface StorageStrategyService {

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getAgentConfig();

    /**
     * This method is used on StorageStrategy implementation when the resource index is associated to a
     * physical interface like frame relay resources. OpenNMS always track changes on ifTable so, make SNMP
     * queries on this table is redundant, and implementations of CollectionAgent know ifTable content always.
     * This method give interface information from a specific ifIndex.
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpInterfaceLabel(int ifIndex);
}
