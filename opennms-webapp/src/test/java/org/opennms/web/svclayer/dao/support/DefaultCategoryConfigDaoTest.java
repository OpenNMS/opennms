/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
/**
 * 
 */
package org.opennms.web.svclayer.dao.support;

import java.util.Collection;
import java.util.Iterator;

import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.mock.MockCategoryFactory;
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
        @Override
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
