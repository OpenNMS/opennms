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
package org.opennms.netmgt.dao.jaxb;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.credentials.Range;
import org.opennms.netmgt.config.wsman.credentials.WsmanConfig;
import org.opennms.netmgt.dao.WSManConfigDao;

public class WSManConfigDaoJaxb extends AbstractJaxbConfigDao<WsmanConfig, WsmanConfig> implements WSManConfigDao {

    public WSManConfigDaoJaxb() {
        super(WsmanConfig.class, "WS-Man Configuration");
    }

    @Override
    public WsmanConfig getConfig() {
        return getContainer().getObject();
    }

    @Override
    public Definition getAgentConfig(InetAddress agentInetAddress) {
        Objects.requireNonNull(agentInetAddress);

        for (Definition def : getConfig().getDefinition()) {
            // Check the specifics first
            for (String saddr : def.getSpecific()) {
                InetAddress addr = InetAddressUtils.addr(saddr);
                if (addr.equals(agentInetAddress)) {
                    return def;
                }
            }

            // Check the ranges
            for (Range rng : def.getRange()) {
                if (InetAddressUtils.isInetAddressInRange(InetAddressUtils.str(agentInetAddress), rng.getBegin(), rng.getEnd())) {
                    return def;
                }
            }

            // Check the matching IP expressions
            for (String ipMatch : def.getIpMatch()) {
                if (IPLike.matches(InetAddressUtils.str(agentInetAddress), ipMatch)) {
                    return def;
                }
            }
        }

        // No definition references the given agent address, use the defaults
        return new Definition(getConfig());
    }

    @Override
    public WSManEndpoint getEndpoint(InetAddress agentInetAddress) {
        return WSManConfigDao.getEndpoint(getAgentConfig(agentInetAddress), agentInetAddress);
    }

    @Override
    protected WsmanConfig translateConfig(WsmanConfig config) {
        return config;
    }
}
