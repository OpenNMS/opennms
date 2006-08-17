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

import org.opennms.netmgt.dao.CategoryStatusDefDao;
import org.opennms.netmgt.model.Category_StatusDef;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

public class SqlMapClientCatStatDefDao extends SqlMapClientDaoSupport implements CategoryStatusDefDao {

	public void delete(int id) {
		getSqlMapClientTemplate().delete("CatStatDef.delete", id);
	}

	public Category_StatusDef findByStatusDefId(int statusDefId) {
		return (Category_StatusDef)getSqlMapClientTemplate().queryForObject("CatStatDef.getByStatDefId", statusDefId);
	}

	public Category_StatusDef find(int id) {
		return (Category_StatusDef)getSqlMapClientTemplate().queryForObject("CatStatDef.getByID", id);
	}

	public List getAll() {
		return getSqlMapClientTemplate().queryForList("CatStatDef.getAll", null);
	}

	public void insert(Category_StatusDef def) {
		// TODO This table is used as the "glue" for a many-to-many we need to figure out how to handle save/insert/update

	}

	public void save(Category_StatusDef def) {
		// TODO This table is used as the "glue" for a many-to-many we need to figure out how to handle save/insert/update
	}

	public void update(Category_StatusDef def) {
		// TODO This table is used as the "glue" for a many-to-many we need to figure out how to handle save/insert/update

	}

}
