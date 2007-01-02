package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsSecretAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

/**
 * This is part of the 'secret' project from the 2005 Dev-Jam.  It will mostly
 * likely be replaced by or refactored into the new ResourceDao. 
 */
public interface AttributeSecretDao {
	
	Collection<OnmsSecretAttribute> getAttributesForNode(OnmsNode node);

	Collection<OnmsSecretAttribute> getAttributesForInterface(OnmsIpInterface iface);

	OnmsSecretAttribute getResponseTimeAttributeForService(OnmsMonitoredService svc);

	//OnmsSecretAttribute getAttribute(String id);
}
