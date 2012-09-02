/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.test.DaoTestConfigBean;

public class SyslogdConfigFactoryTest {

    private SyslogdConfigFactory m_factory;

    @Before
    public void setUp() throws Exception {

        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        MockNetwork network = new MockNetwork();

        MockDatabase db = new MockDatabase();
        db.populate(network);

        DataSourceFactory.setInstance(db);

        m_factory = new SyslogdConfigFactory(ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-configuration.xml"));
    }

    @Test
    public void testSetUp() {
    }

    @Test
    public void testMyHostNameGrouping() {
        Assert.assertEquals(
                6,
                m_factory.getMatchingGroupHost());

    }

    @Test
    public void testMyMessageGroup() {
        Assert.assertEquals(
                8,
                m_factory.getMatchingGroupMessage());

    }

    @Test
    public void testPattern() {
        Assert.assertEquals(
                "^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)",
                m_factory.getForwardingRegexp());
    }

    @Test
    public void testUEI() {
        List<UeiMatch> ueiList = m_factory.getUeiList().getUeiMatchCollection();
        UeiMatch uei = ueiList.get(0);
        Assert.assertEquals("substr", uei.getMatch().getType());
        Assert.assertEquals("CRISCO", uei.getMatch().getExpression());
        Assert.assertEquals("uei.opennms.org/tests/syslogd/substrUeiRewriteTest", uei.getUei());

        uei = ueiList.get(1);
        Assert.assertEquals("regex", uei.getMatch().getType());
        Assert.assertEquals("foo: (\\d+) out of (\\d+) tests failed for (\\S+)$", uei.getMatch().getExpression());
        Assert.assertEquals("uei.opennms.org/tests/syslogd/regexUeiRewriteTest", uei.getUei());
    }

    @Test
    public void testHideTheseMessages() {
        for (HideMatch hide : m_factory.getHideMessages().getHideMatchCollection()) {
            boolean typeOk = ( hide.getMatch().getType().equals("substr") || hide.getMatch().getType().equals("regex") );
            Assert.assertTrue(typeOk);
            if (hide.getMatch().getType().equals("substr")) {
                Assert.assertEquals("TESTHIDING", hide.getMatch().getExpression());
            } else if (hide.getMatch().getType().equals("regex")) {
                Assert.assertEquals("[Dd]ouble[Ss]ecret", hide.getMatch().getExpression());
            }
        }
    }

    @Test
    public void testImportFiles() throws Exception {
        SyslogdConfigFactory factory = new SyslogdConfigFactory(this.getClass().getResourceAsStream("syslogd-configuration-with-imports.xml"));
        Assert.assertEquals(22, factory.getUeiList().getUeiMatchCount());
        Assert.assertEquals(4, factory.getHideMessages().getHideMatchCount());
        int countMatch = 0;
        for (HideMatch hide : factory.getHideMessages().getHideMatchCollection()) {
            if (hide.getMatch().getExpression().startsWith("bad"))
                countMatch++;
        }
        Assert.assertEquals(2, countMatch);
        countMatch = 0;
        for (UeiMatch ueiMatch : factory.getUeiList().getUeiMatchCollection()) {
            if (ueiMatch.getProcessMatch() != null && ueiMatch.getProcessMatch().getExpression().startsWith("agalue"))
                countMatch++;
        }
        Assert.assertEquals(8, countMatch);
    }
}
