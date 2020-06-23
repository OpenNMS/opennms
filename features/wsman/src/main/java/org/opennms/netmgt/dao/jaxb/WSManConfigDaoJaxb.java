/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.wsman.Definition;
import org.opennms.netmgt.config.wsman.Range;
import org.opennms.netmgt.config.wsman.WsmanConfig;
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
