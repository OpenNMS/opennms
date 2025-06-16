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
package org.opennms.web.outage.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.api.Util;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.WebOutageRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class WebOutageRepositoryFilterIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    @Qualifier("dao")
    WebOutageRepository m_daoOutageRepo;
    
    @Autowired
    ApplicationContext m_appContext;
    
    @BeforeClass
    public static void setupLogging(){
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
        OnmsMonitoredService svc2 = m_dbPopulator.getMonitoredServiceDao().get(m_dbPopulator.getNode2().getId(), InetAddressUtils.addr("192.168.2.1"), "ICMP");
        // This requires every test method to have a new database instance :/
        OnmsEvent event = m_dbPopulator.getEventDao().get(1);
        
        OnmsOutage unresolved2 = new OnmsOutage(new Date(), event, svc2);
        m_dbPopulator.getOutageDao().save(unresolved2);
        m_dbPopulator.getOutageDao().flush();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testOutageIdFilter(){
        OutageIdFilter filter = new OutageIdFilter(1);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testLostServiceDateBeforeFilter(){
        LostServiceDateBeforeFilter filter = new LostServiceDateBeforeFilter(new Date());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(5, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testLostServiceDateAfterFilter(){
        LostServiceDateAfterFilter filter = new LostServiceDateAfterFilter(yesterday());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testRegainedServiceDateBeforeFilter(){
        RegainedServiceDateBeforeFilter filter = new RegainedServiceDateBeforeFilter(new Date());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testRegainedServiceDateAfterFilter(){
        OnmsMonitoredService svc2 = m_dbPopulator.getMonitoredServiceDao().get(m_dbPopulator.getNode2().getId(), InetAddressUtils.addr("192.168.2.1"), "ICMP");
        // This requires every test method to have a new database instance :/
        OnmsEvent event = m_dbPopulator.getEventDao().get(1);

        // Put a resolved outage into the database so that one will match the
        // filter below
        OnmsOutage resolvedToday = new OnmsOutage(new Date(), new Date(), event, event, svc2, null, null);
        m_dbPopulator.getOutageDao().save(resolvedToday);
        m_dbPopulator.getOutageDao().flush();

        RegainedServiceDateAfterFilter filter = new RegainedServiceDateAfterFilter(yesterday());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    private static Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DATE, -1 );
        return cal.getTime();
    }

    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testRecentOutagesFilter(){
        RecentOutagesFilter filter = new RecentOutagesFilter();
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }

    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testNegativeInterfaceFilter(){
        NegativeInterfaceFilter filter = new NegativeInterfaceFilter("192.168.2.1");
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(4, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNegativeServiceFilter(){
        NegativeServiceFilter filter = new NegativeServiceFilter(2, null);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        OutageCriteria criteria = new OutageCriteria(filter);
        
        InterfaceFilter filter2 = new InterfaceFilter("192.168.2.1");
        OutageCriteria criteria2 = new OutageCriteria(filter2);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(4, outages.length);
        
        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testServiceFilter(){
        ServiceFilter filter = new ServiceFilter(2, null);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(4, outages.length);
    }

    @Test
    public void testNMS15294() {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest(new MockServletContext(), "POST", "/list.htm");

        httpServletRequest.setParameter("filter", "intf=192.168.1.1");
        httpServletRequest.setParameter("\"><script>alert('foo');</script>", "foo");
        httpServletRequest.setParameter("foo", "\"><script>alert('foo');</script>");

        final Map<String, Object> additions = new HashMap<>();
        additions.put("sortby", "id");
        additions.put("outtype", "current");
        additions.put("limit", "20");

        final String queryString = Util.makeQueryString(
                httpServletRequest,
                additions,
                new String[] { "sortby", "outtype", "limit", "multiple", "filter" },
                Util.IgnoreType.REQUEST_ONLY
        );

        assertThat(queryString, Matchers.not(Matchers.containsString("\"")));
        assertThat(queryString, Matchers.not(Matchers.containsString("'")));
        assertThat(queryString, Matchers.not(Matchers.containsString(">")));
        assertThat(queryString, Matchers.not(Matchers.containsString("<")));
        assertThat(queryString, Matchers.not(Matchers.containsString("<script>")));
        assertThat(queryString, Matchers.not(Matchers.containsString("alert('foo')>")));
    }
}
