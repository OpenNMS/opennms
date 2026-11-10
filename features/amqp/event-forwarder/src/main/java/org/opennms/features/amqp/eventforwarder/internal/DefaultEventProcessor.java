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
package org.opennms.features.amqp.eventforwarder.internal;

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

    public static final String EVENT_HEADER_FOREIGNSOURCE = "foreignSource";
    public static final String EVENT_HEADER_FOREIGNID = "foreignId";

    private NodeDao nodeDao;

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Event event = exchange.getIn().getBody(Event.class);

        // If we want to filter certain Events, we can use the following to stop the exchange
        // exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE); 

        // If applicable, the node's foreign id and foreign source
        // are added as message headers
        if (event.getNodeid() > 0) {
            OnmsNode node = nodeDao.get(event.getNodeid().intValue());

            if (node != null) {
                String foreignSource = node.getForeignSource();
                String foreignId = node.getForeignId();
                if (foreignSource != null && foreignId != null) {
                    exchange.getIn().setHeader(EVENT_HEADER_FOREIGNSOURCE, node.getForeignSource());
                    exchange.getIn().setHeader(EVENT_HEADER_FOREIGNID, node.getForeignId());
                }
            } else {
                LOG.warn("Could not find node {} in the database, cannot add requisition headers", event.getNodeid());
            }
        }

        // Marshall the Event to XML
        exchange.getIn().setBody(JaxbUtils.marshal(event), String.class);
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }
}
