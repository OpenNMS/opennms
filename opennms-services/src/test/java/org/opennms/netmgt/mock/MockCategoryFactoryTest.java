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
package org.opennms.netmgt.mock;

/**
 * @author jsartin
 */

import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.CategoryFactory;
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
	
	protected void setUp() throws Exception {
		super.setUp();
		m_mockCatFactory = new MockCategoryFactory(MOCK_CATEGORY_CONFIG);
		CategoryFactory.setInstance(m_mockCatFactory);
		m_catFactory = CategoryFactory.getInstance();
	}

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
