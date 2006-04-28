package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;

import org.opennms.netmgt.dao.AgentDao;
import org.opennms.netmgt.dao.jdbc.agent.FindAgentsForNode;
import org.opennms.netmgt.dao.jdbc.agent.FindAgentsForNodeOfType;
import org.opennms.netmgt.dao.jdbc.agent.FindAgentsForType;
import org.opennms.netmgt.dao.jdbc.agent.FindAllAgents;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public class AgentDaoJdbc extends AbstractDaoJdbc implements AgentDao {

	public Collection findForType(OnmsServiceType serviceType) {
		return new FindAgentsForType(getDataSource()).findSet(serviceType.getId());
	}

	public int countAll() {
		return findAll().size();
	}

	public void flush() {
	}

	public Collection findForNodeOfType(OnmsNode node, OnmsServiceType serviceType) {
		return new FindAgentsForNodeOfType(getDataSource()).findSet(node.getId(), serviceType.getId());
	}

	public Collection findForNode(OnmsNode node) {
		return new FindAgentsForNode(getDataSource()).findSet(node.getId());
	}

	public Collection findAll() {
		return new FindAllAgents(getDataSource()).findSet();
	}
	

}
