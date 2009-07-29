/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Mar 10: Need to autowire by name now that we have two EventIpcManagers
 *              (the real one and the proxy). - dj@opennms.org
 * 2008 Feb 16: Move testSendEventWithService to JdbcEventWriterTest and make
 *              this test case just about starting up the daemon with default
 *              bean wiring and configs so we can catch problems like bug #2273
 *              in the future. - dj@opennms.org
 * 2008 Feb 10: Fix failing test testSendEventWithService. - dj@opennms.org
 * 2008 Feb 05: Load up a smaller EventConfDao and run one more test. - dj@opennms.org
 * 2008 Jan 27: Enable the service test and make it do something for real. - dj@opennms.org
 * 2008 Jan 26: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
                "classpath*:/META-INF/opennms/component-dao.xml",
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
