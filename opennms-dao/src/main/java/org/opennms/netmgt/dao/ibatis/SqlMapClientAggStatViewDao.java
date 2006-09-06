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

import java.util.Collection;

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

	@SuppressWarnings("unchecked")
	public Collection<AggregateStatusView> findAll() {
		return getSqlMapClientTemplate().queryForList("AggStatView.getAll", null);
	}
	
	public void saveOrUpdate(AggregateStatusView view) {
		if (view.getId() == 0) {
			save(view);
		} else {
			update(view);
		}

	}

	public AggregateStatusView findByName(String name) {
		return (AggregateStatusView)getSqlMapClientTemplate().queryForObject("AggStatView.getByName", name);
	}

	public AggregateStatusView get(Integer id) {
		return (AggregateStatusView)getSqlMapClientTemplate().queryForObject("AggStatView.getByID", id);
	}
	
	public void save(AggregateStatusView view) {
		getSqlMapClientTemplate().insert("AggStatView.insert", view);
	}
	
	public void delete(int id) {
		getSqlMapClientTemplate().delete("AggStatView.delete", new Integer(id));
	}

	public void update(AggregateStatusView view) {
		getSqlMapClientTemplate().update("AggStatView.update", view);
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public int countAll() {
		throw new UnsupportedOperationException("not yet implemented!");
	}

	public void delete(AggregateStatusView entity) {
		delete(entity.getId());
	}

	public void flush() {
		// TODO Auto-generated method stub
		
	}

	public AggregateStatusView load(Integer id) {
		return get(id);
	}
}
