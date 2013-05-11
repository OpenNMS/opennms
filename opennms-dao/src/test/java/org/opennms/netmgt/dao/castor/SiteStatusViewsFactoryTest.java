/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.io.IOException;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.siteStatusViews.View;

public class SiteStatusViewsFactoryTest extends TestCase {
	
	private SiteStatusViewsFactory m_factory;

        @Override
	protected void setUp() throws Exception {
		super.setUp();

		m_factory = new SiteStatusViewsFactory(getClass().getResourceAsStream("/org/opennms/netmgt/config/site-status-views.testdata.xml"));
	}

        @Override
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
