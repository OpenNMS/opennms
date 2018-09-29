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

import org.opennms.netmgt.model.OnmsTopologyConsumer;
import org.opennms.netmgt.model.OnmsTopologyMessage;

public class TopologyLogger implements OnmsTopologyConsumer {

    public static TopologyLogger createAndSubscribe(String protocol, EnhancedLinkd linkd) {
        TopologyLogger tl = new TopologyLogger(protocol);
        linkd.getTopologyDao().subscribe(tl);
        return tl;
    }
    
    private Set<String> m_protocols;
    public TopologyLogger(String protocol) {
        m_protocols = new HashSet<String>();
        m_protocols.add(protocol);
    }

    @Override
    public String getId() {
        return "CDP:Consumer:Logger";
    }

    @Override
    public Set<String> getProtocols() {
        return m_protocols;
    }

    @Override
    public void consume(OnmsTopologyMessage message) {
        System.out.println("received message type:" +  message.getMessagestatus() + " ref:"+message.getMessagebody().getId());
    }

}
