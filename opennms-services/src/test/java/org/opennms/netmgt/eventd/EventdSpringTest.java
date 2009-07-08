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

package org.opennms.netmgt.eventd;

import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.DaoTestConfigBean;

/**
 * Test the startup and shutdown of eventd with the default wiring and
 * configuration files.  Don't override *any* beans so we can see if the
 * daemon will work as it does in production (as possible).
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventdSpringTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private Eventd m_daemon;
    private EventIpcManager m_eventIpcManager;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
        
        super.setAutowireMode(AUTOWIRE_BY_NAME);
    }

    @Override
    protected String[] getConfigLocations() {
        /**
         * Don't put any bean override files in here.  We want to use the
         * default bean files.
         */
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
                "classpath:META-INF/opennms/applicationContext-eventDaemon.xml"
        };
    }

    /**
     * Test the startup and shutdown of this daemon.  This is the only test in
     * this file because having more seems to cause OutOfMemory errors within
     * Eclipse when there are multiple tests due to the large number of events
     * that are loaded by default.
     */
    public void testDaemon() throws Exception {
        assertNotNull("daemon bean", m_daemon);
        
        m_daemon.onStart();
        m_daemon.onStop();
    }
    
    public Eventd getDaemon() {
        return m_daemon;
    }

    public void setDaemon(Eventd daemon) {
        m_daemon = daemon;
    }

    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }
}
