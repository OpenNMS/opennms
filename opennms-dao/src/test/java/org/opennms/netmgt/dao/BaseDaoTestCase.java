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
// Modifications:
//
// 2007 Jul 03: Organize imports. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class BaseDaoTestCase extends
		AbstractTransactionalDataSourceSpringContextTests {

	private DistPollerDao m_distPollerDao;
	private NodeDao m_nodeDao;
	

	protected String[] getConfigLocations() {
        System.setProperty("opennms.home", "src/test/opennms-home");
		return new String[] { "classpath:/META-INF/opennms/applicationContext-dao.xml", "classpath*:/META-INF/opennms/component-dao.xml" };
	}

	public void setDistPollerDao(DistPollerDao distPollerDao) {
		m_distPollerDao = distPollerDao;
	}

	public DistPollerDao getDistPollerDao() {
		return m_distPollerDao;
	}
	
	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}
	
	public NodeDao getNodeDao() {
		return m_nodeDao;
	}
	
	


}
