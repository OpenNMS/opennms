package org.opennms.features.activemq.eventforwarder;

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
public class ForeignSourceForeignIdHeaderProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(ForeignSourceForeignIdHeaderProcessor.class);

	public static final String EVENT_HEADER_FOREIGNSOURCE = "foreignSource";
	public static final String EVENT_HEADER_FOREIGNID = "foreignId";

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
	}
}
