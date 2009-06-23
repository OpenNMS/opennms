//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.config;

import java.io.IOException;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;

public class SiteStatusViewsFactoryTest extends TestCase {
	
	private SiteStatusViewsFactory m_factory;

	protected void setUp() throws Exception {
		super.setUp();
		
		MockNetwork network = new MockNetwork();
		
		MockDatabase db = new MockDatabase();
		db.populate(network);
		
		DataSourceFactory.setInstance(db);

		m_factory = new SiteStatusViewsFactory(getClass().getResourceAsStream("/org/opennms/netmgt/config/site-status-views.testdata.xml"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGetName() throws MarshalException, ValidationException, IOException {
		String viewName = "default";
		View view = m_factory.getView(viewName);
		assertNotNull(view);
		assertEquals(viewName, view.getName());
        
        assertEquals(5, view.getRows().getRowDefCount());
	}

}
