package org.opennms.features.activemq.eventreceiver;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
public class UpdateNodeIdByForeignSourceForeignIdHeaderProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(UpdateNodeIdByForeignSourceForeignIdHeaderProcessor.class);

	public static final String EVENT_HEADER_FOREIGNSOURCE = "foreignSource";
	public static final String EVENT_HEADER_FOREIGNID = "foreignId";
	public static final String EVENT_HEADER_LOCATION = "location";
	

	private NodeDao nodeDao;

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Event event = exchange.getIn().getBody(Event.class);
		
		String from = exchange.getIn().getHeader(EVENT_HEADER_LOCATION, String.class);

		if (event.getNodeid() > 0) {
			String foreignSource = exchange.getIn().getHeader(EVENT_HEADER_FOREIGNSOURCE, String.class);
			String foreignId = exchange.getIn().getHeader(EVENT_HEADER_FOREIGNID, String.class);

			OnmsNode node = nodeDao.findByForeignId(foreignSource, foreignId);

			if (node != null && node.getId() != null) {
				event.setNodeid(node.getId().longValue());
				event.setDistPoller(from);
				event.setSource("Endpoint="+from+":"+event.getSource());
			} else {
				LOG.warn("Could not find node {}/{} in the database, cannot update node ID to local value; discarding event", foreignSource, foreignId);
				// Halt the route if we cannot translate the node ID
				exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
			}
		}
	}
}
