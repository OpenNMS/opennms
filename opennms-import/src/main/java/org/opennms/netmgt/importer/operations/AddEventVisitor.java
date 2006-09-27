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

public final class AddEventVisitor extends AbstractEntityVisitor {
	private final List m_events;

	AddEventVisitor(List events) {
		m_events = events;
	}

	public void visitNode(OnmsNode node) {
		m_events.add(EventUtils.createNodeAddedEvent(node.getId().intValue(), node.getLabel(), node.getLabelSource()));
	}

	public void visitIpInterface(OnmsIpInterface iface) {
		m_events.add(EventUtils.createNodeGainedInterfaceEvent("ModelImporter", iface.getNode().getId().intValue(), iface.getInetAddress()));
	}

	public void visitMonitoredService(OnmsMonitoredService monSvc) {
		OnmsIpInterface iface = monSvc.getIpInterface();
		OnmsNode node = iface.getNode();
		m_events.add(EventUtils.createNodeGainedServiceEvent("ModelImporter", monSvc.getNodeId().intValue(), iface.getInetAddress(), monSvc.getServiceType().getName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription()));
	}
}