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
package org.opennms.netmgt.collectd;

import java.util.Set;

import org.opennms.netmgt.collection.api.CollectionAgentService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface SnmpCollectionAgentService extends CollectionAgentService {

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    int getIfIndex();

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getSysObjectId();

    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    PrimaryType getIsSnmpPrimary();

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    SnmpAgentConfig getAgentConfig();

    /**
     * <p>getSnmpInterfaceData</p>
     *
     * @return a {@link java.util.Set} object.
     */
    Set<SnmpIfData> getSnmpInterfaceData();

}
