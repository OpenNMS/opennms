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
package org.opennms.netmgt.config;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.test.DaoTestConfigBean;

public class SyslogdConfigFactoryIT {

    private SyslogdConfigFactory m_factory;

    @Before
    public void setUp() throws Exception {

        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        DataSource ds = Mockito.mock(DataSource.class);
        DataSourceFactory.setInstance(ds);

        m_factory = new SyslogdConfigFactory(ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-configuration.xml"));
    }

    @Test
    public void testSetUp() {
    }

    @Test
    public void testMyHostNameGrouping() {
        Assert.assertEquals(
                Integer.valueOf(6),
                m_factory.getMatchingGroupHost());

    }

    @Test
    public void testMyMessageGroup() {
        Assert.assertEquals(
                Integer.valueOf(8),
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
        List<UeiMatch> ueiList = m_factory.getUeiList();
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
        for (final HideMatch hide : m_factory.getHideMessages()) {
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
        Assert.assertEquals(22, factory.getUeiList().size());
        Assert.assertEquals(4, factory.getHideMessages().size());
        int countMatch = 0;
        for (final HideMatch hide : factory.getHideMessages()) {
            if (hide.getMatch().getExpression().startsWith("bad"))
                countMatch++;
        }
        Assert.assertEquals(2, countMatch);
        countMatch = 0;
        for (UeiMatch ueiMatch : factory.getUeiList()) {
            if (ueiMatch.getProcessMatch().isPresent() && ueiMatch.getProcessMatch().get().getExpression().startsWith("agalue"))
                countMatch++;
        }
        Assert.assertEquals(8, countMatch);
    }
}
