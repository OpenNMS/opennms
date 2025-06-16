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
		assertEquals(99,category.getNormalThreshold(),0);
		assertEquals(97,category.getWarningThreshold(),0);
		assertTrue(category.getComment().isPresent());
		assertEquals(CATCOMMENT,category.getComment().get());
		assertEquals(CATRULE,category.getRule());
		assertEquals("ICMP",category.getServices().get(0));
		assertEquals("SNMP",category.getServices().get(1));
		
		
	}

	public void testGetEffectiveRule() {
		assertEquals(EFFECTIVE_RULE,m_catFactory.getEffectiveRule(CATLABEL));
	}

	

}
