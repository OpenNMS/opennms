/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.credentials.Range;
import org.opennms.netmgt.config.wsman.credentials.WsmanConfig;
import org.opennms.netmgt.dao.WSManConfigDao;

public class DefaultWSManConfigDao extends AbstractCmJaxbConfigDao<WsmanConfig> implements WSManConfigDao {

    private static final String CONFIG_NAME = "wsman";
    private static final String DEFAULT_CONFIG_ID = "default";

    public DefaultWSManConfigDao() {
        super(WsmanConfig.class, "WS-Man Configuration");
    }

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return DEFAULT_CONFIG_ID;
    }

    @Override
    public WsmanConfig getConfig() {
        return this.getConfig(this.getDefaultConfigId());
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

}
