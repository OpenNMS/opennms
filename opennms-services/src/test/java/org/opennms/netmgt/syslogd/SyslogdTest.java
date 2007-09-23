// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 24: Fix failing tests and warnings. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.syslogd;

import java.net.UnknownHostException;

import org.apache.log4j.Level;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

public class SyslogdTest extends OpenNMSTestCase {

    private Syslogd m_syslogd;

    public SyslogdTest() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources/org/opennms/netmgt/test-configurations/opennms");
        daoTestConfig.afterPropertiesSet();
    }

    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        MockNetwork network = new MockNetwork();
        MockDatabase db = new MockDatabase();
        db.populate(network);
        DataSourceFactory.setInstance(db);
        Reader rdr = new InputStreamReader(getClass().getResourceAsStream( "/etc/syslogd-configuration.xml"));

        m_factory = new SyslogdConfigFactory(rdr);
        rdr.close();

        m_syslogd = new Syslogd();
        m_syslogd.init();
    }

    @Override
    protected void tearDown() throws Exception {
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNotGreaterOrEqual(Level.FATAL);
    }

    public void testSyslogdStart() {
        assertEquals("START_PENDING", m_syslogd.getStatusText());
        m_syslogd.start();
    }

    public void testMessaging() {
        // More of an integrations test
        // relies on you reading some of the logging....

        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 0, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_ERR, "Hello.");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }

    }

    public void testMyPatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 host.domain.com A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }

        //LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        //assertEquals("number of logged events", 0, events.length);
        //assertEquals("first logged event severity (should be ERROR)", Level.ERROR, events[0].getLevel());

        MockLogAppender.resetEvents();
        MockLogAppender.resetLogLevel();
    }

    public void testIPPatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 127.0.0.1 A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }

    public void testResolvePatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 www.opennms.org A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }

    public void testUEIRewrite() {
        //uei.opennms.org/internal/discovery/newCriscoRouter
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 www.opennms.org A CISCO message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }

    public void testTESTTestThatRemovesATESTString() {
        //uei.opennms.org/internal/discovery/newCriscoRouter
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 www.opennms.org A TEST message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }
}
