//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 24: Fix forwarding regexp, use Java 5 generics and for loops. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.Reader;

import junit.framework.TestCase;

import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;

public class SyslogdConfigFactoryTest extends TestCase {
    private SyslogdConfigFactory m_factory;
    
    public SyslogdConfigFactoryTest() {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();
    }

    protected void setUp() throws Exception {
        super.setUp();

        MockNetwork network = new MockNetwork();

        MockDatabase db = new MockDatabase();
        db.populate(network);

        DataSourceFactory.setInstance(db);

        Reader rdr = ConfigurationTestUtils.getReaderForResource(this, "/etc/syslogd-configuration.xml");
        m_factory = new SyslogdConfigFactory(rdr);
        rdr.close();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetUp() {
    }

    public void testMyHostNameGrouping() {
        assertEquals(
                6,
                m_factory.getMatchingGroupHost());

    }

    public void testMyMessageGroup() {
        assertEquals(
                8,
                m_factory.getMatchingGroupMessage());

    }

    public void testPattern() {
        assertEquals(
                "^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)",
                m_factory.getForwardingRegexp());
    }

    public void testUEI() {
        for (UeiMatch uei : m_factory.getUeiList().getUeiMatchCollection()) {
            assertTrue( ( "substr".equals(uei.getMatch().getType()) ) || ( "regex".equals(uei.getMatch().getType()) ) );
            if (uei.getMatch().getType().equals("substr")) {
                assertEquals("CRISCO", uei.getMatch().getExpression());
            } else if (uei.getMatch().getType().equals("regex")) {
                assertEquals("uei.opennms.org/tests/syslogd/substrUeiRewriteTest", uei.getUei());
            }
        }
    }

    public void testHideTheseMessages() {
        for (HideMatch hide : m_factory.getHideMessages().getHideMatchCollection()) {
            assertTrue( ( "substr".equals(hide.getMatch().getType()) ) || ( "regex".equals(hide.getMatch().getType()) ) );
            if (hide.getMatch().getType().equals("substr")) {
                assertEquals("TEST", hide.getMatch().getExpression());
            } else if (hide.getMatch().getType().equals("regex")) {
                assertEquals("[Dd]ouble secret", hide.getMatch().getExpression());
            }
        }
    }

}
