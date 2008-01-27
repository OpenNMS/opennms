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
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.test.DaoTestConfigBean;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventdSpringTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private Eventd m_daemon;
    private EventIpcManager m_eventIpcManager;
    
    public EventdSpringTest() {
        super();

        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/applicationContext-eventDaemon.xml"
        };
    }
    
    public void testDaemonNonNull() throws Exception {
        assertNotNull("daemon bean", m_daemon);
        m_daemon.onStart();
    }
    
    // FIXME: I get OutOfMemory errors if I run more than one test in this TestCase
    public void FIXMEtestSendEvent() throws Exception {
        EventBuilder builder = new EventBuilder("uei.opennms.org/foo", "someSource");
        m_eventIpcManager.sendNow(builder.getEvent());
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
