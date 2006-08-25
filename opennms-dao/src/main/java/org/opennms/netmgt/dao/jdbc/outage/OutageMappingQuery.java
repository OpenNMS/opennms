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
package org.opennms.netmgt.dao.jdbc.outage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.jdbc.object.MappingSqlQuery;

public class OutageMappingQuery extends MappingSqlQuery {

	private OutageMapper m_outageMapper;

	public OutageMappingQuery(DataSource ds, String clause) {
		super(ds, "SELECT " 
				+ "outages.outageID as outages_outageID, "
				+ "outages.svcLostEventID as outages_svcLostEventID, "
				+ "outages.svcRegainedEventID as outages_svcRegainedEventID, "
				+ "outages.nodeID as outages_nodeID, "
				+ "outages.ipAddr as outages_ipAddr, "
				+ "outages.serviceID as outages_serviceID, "
				+ "ifservices.ifIndex as outages_ifIndex, "
				+ "outages.ifLostService as outages_ifLostService, "
				+ "outages.ifRegainedService as outages_ifRegainedService, "
				+ "outages.suppressTime as outages_suppressTime, "
				+ "outages.suppressedBy as outages_suppressedBy "
				+ clause);
		m_outageMapper = new OutageMapper();
	}

	public DataSource getDataSource() {
		return getJdbcTemplate().getDataSource();
	}

	public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return m_outageMapper.mapRow(rs, rowNumber);
	}

	public OnmsOutage findUnique() {
		return findUnique((Object[]) null);
	}

	public OnmsOutage findUnique(Object obj) {
		return findUnique(new Object[] { obj });
	}

	public OnmsOutage findUnique(Object[] objs) {
		List events = execute(objs);
		if (events.size() > 0)
			return (OnmsOutage) events.get(0);
		else
			return null;
	}

	public Set<OnmsOutage> findSet() {
		return findSet((Object[]) null);
	}

	public Set<OnmsOutage> findSet(Object obj) {
		return findSet(new Object[] { obj });
	}

	@SuppressWarnings("unchecked")
	public Set<OnmsOutage> findSet(Object[] objs) {
		List events = execute(objs);
		Set<OnmsOutage> results = new JdbcSet(events);
		return results;
	}

}