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
