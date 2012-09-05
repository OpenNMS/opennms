/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class DataLinkInterfaceRestServiceTest extends AbstractSpringJerseyRestTestCase {
	private DatabasePopulator m_databasePopulator;

	@Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
		final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		m_databasePopulator = context.getBean("databasePopulator", DatabasePopulator.class);
        m_databasePopulator.populateDatabase();
	}

	@Test
	public void testLinks() throws Exception {
		String xml = sendRequest(GET, "/links",200);
		assertTrue(xml.contains("<links count=\"3\""));
	}

	@Test
	public void testLink() throws Exception {
            String xml = sendRequest(GET, "/links/64",200);
            assertTrue(xml.contains("<link id=\"64\">"));

            xml = sendRequest(GET, "/links/65",200);
            assertTrue(xml.contains("<link id=\"65\">"));

            xml = sendRequest(GET, "/links/66",200);
            assertTrue(xml.contains("<link id=\"66\">"));

	}
	
	@Test
	public void testQueryWithNodeid() throws Exception {
            String xml = sendRequest(GET, "/links", parseParamData("node.id=2"), 200);
            assertTrue(xml.contains("<links count=\"1\""));
	}
	
       @Test
        public void testQueryWithIfIndex() throws Exception {
            String xml = sendRequest(GET, "/links", parseParamData("ifIndex=1"), 200);
            assertTrue(xml.contains("<links count=\"2\""));
        }

       @Test
       public void testQueryWithParentNodeid() throws Exception {
           String xml = sendRequest(GET, "/links", parseParamData("nodeParentId=2"), 200);
           assertTrue(xml.contains("<links count=\"0\""));
       }
       
       @Test
       public void testQueryWithParentIfindex() throws Exception {
           String xml = sendRequest(GET, "/links", parseParamData("parentIfIndex=1"), 200);
           assertTrue(xml.contains("<links count=\"3\""));
       }

       @Test
       public void testQueryWithStatus() throws Exception {
           String xml = sendRequest(GET, "/links", parseParamData("status=A"), 200);
           assertTrue(xml.contains("<links count=\"3\""));
       }

}
