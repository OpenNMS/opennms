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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/**
 * 
 */
package org.opennms.web.svclayer.dao.support;

import java.util.Collection;
import java.util.Iterator;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.mock.MockCategoryFactory;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

import junit.framework.TestCase;

/**
 * @author jsartin
 *
 */
public class DefaultCategoryConfigDaoTest extends TestCase {
	
	DefaultCategoryConfigDao m_dao;
	protected MockCategoryFactory m_catFactory;

	/**
	 * @param arg0
	 */
	public DefaultCategoryConfigDaoTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();
		m_catFactory = new MockCategoryFactory();
		CategoryFactory.setInstance(m_catFactory);
		m_dao = new DefaultCategoryConfigDao();
	}

	   @Override
	    public void runTest() throws Throwable {
	        super.runTest();
	        MockLogAppender.assertNoWarningsOrGreater();
	        MockUtil.println("------------ End Test "+getName()+" --------------------------");
	    }

	    public void testNothing() {
	        // test that setUp() / tearDown() works
	    }
	    
	/**
	 * Test method for {@link org.opennms.web.svclayer.dao.support.DefaultCategoryConfigDao#findAll()}.
	 */
	public void testFindAll() {
		Collection<Category> catColl = m_dao.findAll();
		assertFalse(catColl.isEmpty());
		assertEquals(catColl.size(),2);
		Iterator<Category> i = catColl.iterator();
		assertEquals(i.next().getLabel(),"Network Interfaces");
		assertEquals(i.next().getLabel(),"Web Servers");
		/*Iterator i = list.iterator();
		while(i.hasNext()) {
			Category cat = (Category)i.next();
			MockUtil.println("found a category --" + cat.getLabel());
			
		}*/
	}

}
