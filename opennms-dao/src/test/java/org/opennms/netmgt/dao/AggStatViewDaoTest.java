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

package org.opennms.netmgt.dao;

import org.opennms.netmgt.model.AggregateStatusView;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class AggStatViewDaoTest extends
		AbstractTransactionalDataSourceSpringContextTests {

	AggregateStatusViewDao m_dao;

	public AggregateStatusViewDao getDao() {
		return m_dao;
	}

	public void setDao(AggregateStatusViewDao dao) {
		m_dao = dao;
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml"
        		};
	}

        public void testBogus() {
                // do nothing... we're here so JUnit doesn't complain
        }

	public void FIXMEtestDeleteInsertSave() {
		String randomName = "junit test "+ Math.random();
		AggregateStatusView view = new AggregateStatusView();
		view.setName(randomName);
		view.setTableName("junit test table name");
		view.setColumnName("junit test column name");
		view.setColumnValue("junit test column value");
		
		m_dao.save(view);
		AggregateStatusView retrievedView = m_dao.findByName(randomName);
		assertEquals(view.getTableName(), retrievedView.getTableName());
		
		m_dao.delete(retrievedView);
		retrievedView = m_dao.findByName(randomName);
		assertNull("Expected a null view because we should have just deleted it" +
				" from the table", retrievedView);
		
		m_dao.save(view);
		retrievedView = m_dao.findByName(randomName);
		int newId = retrievedView.getId();
		retrievedView.setName("Modified " + randomName);
		m_dao.saveOrUpdate(retrievedView);
		retrievedView = m_dao.get(newId);
		assertEquals("Modified " + randomName, retrievedView.getName());
		
	}
	
}
