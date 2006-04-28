package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public interface AgentDao extends OnmsDao {
	
	Collection findForType(OnmsServiceType serviceType);
	
	Collection findForNode(OnmsNode node);
	
	Collection findForNodeOfType(OnmsNode node, OnmsServiceType type);
	
	Collection findAll();

}
