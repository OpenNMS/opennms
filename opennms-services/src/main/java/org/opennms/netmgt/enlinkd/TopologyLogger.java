/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.OnmsTopologyConsumer;
import org.opennms.netmgt.model.OnmsTopologyMessage;
import org.opennms.netmgt.model.OnmsTopologyProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TopologyLogger implements OnmsTopologyConsumer {

    public static TopologyLogger createAndSubscribe(OnmsTopologyProtocol protocol) {
        TopologyLogger tl = new TopologyLogger(protocol);
        Assert.assertNotNull(tl.getTopologyDao());
        tl.getTopologyDao().subscribe(tl);
        return tl;
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryCdpTopology.class);

    @Autowired
    private TopologyDao m_topologyDao;

    private Set<OnmsTopologyProtocol> m_protocols;
    public TopologyLogger(OnmsTopologyProtocol protocol) {
        m_protocols = new HashSet<OnmsTopologyProtocol>();
        m_protocols.add(protocol);
    }

    @Override
    public String getId() {
        return "CDP:Consumer:Logger";
    }

    @Override
    public Set<OnmsTopologyProtocol> getProtocols() {
        return m_protocols;
    }

    @Override
    public void consume(OnmsTopologyMessage message) {
        LOG.warn("consume: received message type: {} ref: {}", message.getMessagestatus(), message.getMessagebody().getId());
    }

    public TopologyDao getTopologyDao() {
        return m_topologyDao;
    }

    public void setTopologyDao(TopologyDao topologyDao) {
        m_topologyDao = topologyDao;
    }

}
