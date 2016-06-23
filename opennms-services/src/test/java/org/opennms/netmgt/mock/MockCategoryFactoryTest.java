/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

/**
 * @author jsartin
 */

import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Category;

import junit.framework.TestCase;

public class MockCategoryFactoryTest extends TestCase {

	protected MockCategoryFactory m_mockCatFactory;
	protected CatFactory m_catFactory;
	
	private static final String CATLABEL = "Network Interfaces";
	private static final String CATRULE = "(isICMP | isSNMP) & (ipaddr != \"0.0.0.0\")";
	private static final String EFFECTIVE_RULE = "(ipaddr IPLIKE *.*.*.*) & ((isICMP | isSNMP) & (ipaddr != \"0.0.0.0\"))";
	private static final String CATCOMMENT = "This is a very simple category";
	private static final String MOCK_CATEGORY_CONFIG = 
		"<catinfo>" +
	    " <header>" +
	    "  <rev>1.3</rev>" +
	    "  <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>" +
	    "  <mstation>checkers</mstation>" +
	    " </header>" +
	    " <categorygroup>" +
	    "  <name>WebConsole</name>" +
	    "  <comment>Service Level Availability by Functional Group</comment>" +
	    "  <common>" +
	    "   <rule><![CDATA[ipaddr IPLIKE *.*.*.*]]></rule>" +
	    "  </common>" +
	    "  <categories>" +
	    "   <category>" +
	    "    <label><![CDATA[Network Interfaces]]></label>" +
	    "    <comment>This is a very simple category</comment>" +
	    "    <normal>99</normal>" +
	    "    <warning>97</warning>" +
	    "    <service>ICMP</service>" +
	    "    <service>SNMP</service>" +
	    "    <rule><![CDATA[(isICMP | isSNMP) & (ipaddr != \"0.0.0.0\")]]></rule>" +
		"   </category>" +
		"  </categories>" +
		" </categorygroup>" +
		"</catinfo>";
	
        @Override
	protected void setUp() throws Exception {
		super.setUp();
		m_mockCatFactory = new MockCategoryFactory(MOCK_CATEGORY_CONFIG);
		CategoryFactory.setInstance(m_mockCatFactory);
		m_catFactory = CategoryFactory.getInstance();
	}

        @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	

	public void testGetCategory() {
		
		Category category = m_catFactory.getCategory(CATLABEL);
		assertEquals(99,category.getNormal(),0);
		assertEquals(97,category.getWarning(),0);
		assertEquals(CATCOMMENT,category.getComment());
		assertEquals(CATRULE,category.getRule());
		assertEquals("ICMP",category.getService(0));
		assertEquals("SNMP",category.getService(1));
		
		
	}

	public void testGetEffectiveRule() {
		assertEquals(EFFECTIVE_RULE,m_catFactory.getEffectiveRule(CATLABEL));
	}

	

}
