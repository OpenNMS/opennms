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
