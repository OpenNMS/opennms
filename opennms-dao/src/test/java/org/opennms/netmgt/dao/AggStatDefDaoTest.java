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

import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class AggStatDefDaoTest extends
		AbstractDependencyInjectionSpringContextTests {

	AggregateStatusDefinitionDao m_dao;

	public AggregateStatusDefinitionDao getDao() {
		return m_dao;
	}

	public void setDao(AggregateStatusDefinitionDao dao) {
		m_dao = dao;
	}
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] {
                "applicationContext-ibatis.xml"
        		};
	}

	public void xxtestDeleteInsertSave() {
		String randomName = "junit test "+ Math.random();
		AggregateStatusDefinition def = new AggregateStatusDefinition();
		def.setName(randomName);
		
		m_dao.insert(def);
		AggregateStatusDefinition retrievedDef = m_dao.find(randomName);
		assertEquals(def.getName(), retrievedDef.getName());
		
		m_dao.delete(def);
		retrievedDef = m_dao.find(randomName);
		assertNull("Expected a null view because we should have just deleted it" +
				" from the table", retrievedDef);
		
		m_dao.insert(def);
		retrievedDef = m_dao.find(randomName);
		int newId = retrievedDef.getId();
		retrievedDef.setName("Modified " + randomName);
		m_dao.save(retrievedDef);
		retrievedDef = m_dao.find(newId);
		assertEquals("Modified " + randomName, retrievedDef.getName());
		
	}
	
}
