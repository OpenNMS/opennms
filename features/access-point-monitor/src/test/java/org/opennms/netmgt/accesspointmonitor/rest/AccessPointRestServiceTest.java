/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.accesspointmonitor.rest;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.opennms.core.test.MockLogAppender;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AccessPointRestServiceTest extends AbstractSpringJerseyRestTestCase {
    private AccessPointDatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
        final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        m_databasePopulator = context.getBean("accessPointDatabasePopulator", AccessPointDatabasePopulator.class);
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void testAccessPoints() throws Exception {
        String xml = sendRequest(GET, "/accesspoints", parseParamData("limit=1"), 200);
        assertTrue(xml.contains(m_databasePopulator.getAP1().getPhysAddr()));
    }
}
