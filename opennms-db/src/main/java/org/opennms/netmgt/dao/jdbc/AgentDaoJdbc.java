//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc;

import java.util.Collection;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.AgentDao;
import org.opennms.netmgt.dao.jdbc.agent.FindAgentsForNode;
import org.opennms.netmgt.dao.jdbc.agent.FindAgentsForNodeOfType;
import org.opennms.netmgt.dao.jdbc.agent.FindAgentsForType;
import org.opennms.netmgt.dao.jdbc.agent.FindAllAgents;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public class AgentDaoJdbc extends AbstractDaoJdbc implements AgentDao {

	public AgentDaoJdbc(DataSource dataSource) {
		super(dataSource);
	}

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
