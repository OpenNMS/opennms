/**
 * 
 */
package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

public final class DeleteEventVisitor extends AbstractEntityVisitor {
	private final List m_events;

	DeleteEventVisitor(List events) {
		m_events = events;
	}

	public void visitMonitoredServiceComplete(OnmsMonitoredService monSvc) {
		m_events.add(EventUtils.createServiceDeletedEvent("ModelImporter", monSvc.getNodeId().longValue(), monSvc.getIpAddress(), monSvc.getServiceType().getName(), -1L));
	}

	public void visitIpInterfaceComplete(OnmsIpInterface iface) {
		m_events.add(EventUtils.createInterfaceDeletedEvent("ModelImporter", iface.getNode().getId().longValue(), iface.getIpAddress(), -1L));
	}

	public void visitNodeComplete(OnmsNode node) {
		m_events.add(EventUtils.createNodeDeletedEvent("ModelImporter", node.getId().intValue(), node.getLabel(), node.getLabel(), -1L));
	}
}