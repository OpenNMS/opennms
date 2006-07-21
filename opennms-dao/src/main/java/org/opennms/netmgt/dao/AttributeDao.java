package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

public interface AttributeDao {
	
	Collection<OnmsAttribute> getAttributesForNode(OnmsNode node);

	Collection<OnmsAttribute> getAttributesForInterface(OnmsIpInterface iface);

	OnmsAttribute getResponseTimeAttributeForService(OnmsMonitoredService svc);

	//Attribute getAttribute(String id);
}
