/*
 * This file is part of the OpenNMS(R) Application.
 * 
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jan 26: Don't call methods directly on Eventd to send events
 *              (they are moving, anyway)--use EventIpcManager. - dj@opennms.org
 * 2007 Jun 10: Use SimpleJdbcTemplate for queries. - dj@opennms.org
 * 2007 Jun 09: Move the config into a test resource. - dj@opennms.org
 * 2007 Mar 13: VacuumdConfigFactory.setConfigReader(Reader) is gone.  Use new VacuumdConfigFactory(Reader) and setInstance, instead. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.                                                            
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *    
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.openoss.opennms.spring.qosd;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.openoss.opennms.spring.qosdrx.QoSDrx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * Tests Vacuumd's execution of statements and automations
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @author <a href=mailto:brozow@opennms.org>Mathew Brozowski</a>
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	OpenNMSConfigurationExecutionListener.class,
	TemporaryDatabaseExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class
})
/*
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
*/
@ContextConfiguration
@JUnitTemporaryDatabase
public class QoSDTest {
	//@Autowired
	private QoSDrx m_qosdrx;

	@Autowired
	private QoSD m_qosd;

	@Before
	public void setUp() throws Exception {
		/*
		System.setProperty("opennms.home", "target/test-classes");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
			"META-INF/opennms/applicationContext-dao.xml",
			"META-INF/opennms/component-dao.xml",
			"org/openoss/opennms/spring/qosd/qosd-spring-context.xml"
		});
		m_qosd = context.getBean(QoSD.class);
		m_qosdrx = context.getBean(QoSDrx.class);
		*/
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testContext() {
		// assertEquals("target/test-classes", System.getProperty("opennms.home"));
		//assertNotNull(m_qosdrx);
		assertNotNull(m_qosd);
	}
}
