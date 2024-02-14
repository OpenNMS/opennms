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
package org.opennms.netmgt.dao.jaxb;

import java.io.IOException;

import org.opennms.netmgt.config.siteStatusViews.View;

import junit.framework.TestCase;

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
	
	public void testGetName() throws IOException {
		String viewName = "default";
		View view = m_factory.getView(viewName);
		assertNotNull(view);
		assertEquals(viewName, view.getName());
        
        assertEquals(5, view.getRows().size());
	}

}
