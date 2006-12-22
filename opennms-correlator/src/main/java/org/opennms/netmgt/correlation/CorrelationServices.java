package org.opennms.netmgt.correlation;

import java.util.List;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;

public interface CorrelationServices extends StateActions {
	
	OnmsNode findNodeForEvent(Event e);
	
	OnmsNode findNode(Integer nodeId);
	OnmsNode findParentOfNode(Integer nodeId);

	List<OnmsMonitoredService> getCriticalServicesForNode(Integer nodeId);

	int getCurrentStatusOfNode(Integer nodeId);
	int getCurrentStatusOfService(Integer monSvcId);
	
}
