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
