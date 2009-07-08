/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt;

import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.test.DaoTestConfigBean;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class OpenNmsDaemonApplicationContextTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private EventIpcManager m_eventIpcManager;
    
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml"
        };
    }
    
    public void testEventIpcManagerNonNull() throws Exception {
        assertNotNull("eventIpcManager bean", m_eventIpcManager);
    }

    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }
}
