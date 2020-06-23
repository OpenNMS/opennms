/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.amqp.eventreceiver.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventProcessor implements Processor {
    public static final Logger LOG = LoggerFactory.getLogger(DefaultEventProcessor.class);

    public static final String EVENT_HEADER_SYSTEMID = "systemId";
    public static final String EVENT_HEADER_FOREIGNSOURCE = "foreignSource";
    public static final String EVENT_HEADER_FOREIGNID = "foreignId";

    private NodeDao nodeDao;

    @Override
    public void process(final Exchange exchange) throws Exception {
        final String eventXml = exchange.getIn().getBody(String.class);
        final Event event = JaxbUtils.unmarshal(Event.class, eventXml);
        final String systemId = exchange.getIn().getHeader(EVENT_HEADER_SYSTEMID, String.class);

        if (event.getNodeid() > 0) {
            String foreignSource = exchange.getIn().getHeader(EVENT_HEADER_FOREIGNSOURCE, String.class);
            String foreignId = exchange.getIn().getHeader(EVENT_HEADER_FOREIGNID, String.class);

            OnmsNode node = nodeDao.findByForeignId(foreignSource, foreignId);

            if (node != null && node.getId() != null) {
                event.setNodeid(node.getId().longValue());
                event.setDistPoller(systemId);
                event.setSource("Endpoint="+systemId+":"+event.getSource());
            } else {
                LOG.warn("Could not find node {}/{} in the database, cannot update node ID to local value; discarding event", foreignSource, foreignId);
                // Halt the route if we cannot translate the node ID
                exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
            }
        }

        exchange.getIn().setBody(event, Event.class);
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }
}
