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

package org.opennms.netmgt.dao.ibatis;

import java.util.List;

import org.opennms.netmgt.dao.AggregateStatusViewDao;
import org.opennms.netmgt.model.AggregateStatusView;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

public class SqlMapClientAggStatViewDao extends SqlMapClientDaoSupport
		implements AggregateStatusViewDao {

//	@Override
//	protected void checkDaoConfig() throws IllegalArgumentException {
//		// TODO Auto-generated method stub
//
//	}

//	public void delete(AggregateStatusView view) {
//		
//	}

	public List getAll() {
		return getSqlMapClientTemplate().queryForList("AggStatView.getAll", null);
	}
	
	public void save(AggregateStatusView view) {
		if (view.getId() == 0) {
			insert(view);
		} else {
			update(view);
		}

	}

	public AggregateStatusView find(String name) {
		return (AggregateStatusView)getSqlMapClientTemplate().queryForObject("AggStatView.getByName", name);
	}

	public AggregateStatusView find(int id) {
		return (AggregateStatusView)getSqlMapClientTemplate().queryForObject("AggStatView.getByID", id);
	}
	
	public void insert(AggregateStatusView view) {
		getSqlMapClientTemplate().insert("AggStatView.insert", view);
	}
	
	public void delete(int id) {
		getSqlMapClientTemplate().delete("AggStatView.delete", new Integer(id));
	}

	public void update(AggregateStatusView view) {
		getSqlMapClientTemplate().update("AggStatView.update", view);
	}
}
