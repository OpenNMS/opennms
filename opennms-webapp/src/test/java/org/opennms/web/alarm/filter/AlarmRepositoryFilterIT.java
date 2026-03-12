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
package org.opennms.web.alarm.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmQueryParms;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@DirtiesContext // XXX needed? JUnitTemporaryDatabase marks dirty by default
public class AlarmRepositoryFilterIT implements InitializingBean {

    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    AlarmRepository m_daoAlarmRepo;

    @Autowired
    CategoryDao m_categoryDao;

    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    ApplicationContext m_appContext;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAlarmTypeFilter(){
        OnmsAlarm[] alarm = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmTypeFilter(3))));
        assertEquals(0, alarm.length);
        
        alarm = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new AlarmTypeFilter(1))));
        assertEquals(1, alarm.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testBeforeFirstEventTimeFilter(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new BeforeFirstEventTimeFilter(new Date()))));
        assertEquals(1, alarms.length);

        alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date()))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testBeforeLastEventTime(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new BeforeLastEventTimeFilter(new Date()))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testExactUeiFilter(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new ExactUEIFilter("test uei"))));
        assertEquals(0, alarms.length);
        
        alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new ExactUEIFilter("uei.opennms.org/test"))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testInterfaceFilter(){
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new InterfaceFilter(addr("192.168.1.1")))));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeAcknowledgeByFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new NegativeAcknowledgedByFilter("non user"));
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testIPLikeFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new IPAddrLikeFilter("192.168.1.1"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
        assertEquals(addr("192.168.1.1"), alarms[0].getIpAddr());
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeInterfaceFilter(){
        AlarmCriteria criteria = new AlarmCriteria(new NegativeInterfaceFilter(addr("192.168.1.101")));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeNodeFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeNodeFilter(11, m_appContext));
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
        
        NegativeNodeFilter filter = new NegativeNodeFilter(11, m_appContext);
        assertEquals("node is not 11", filter.getTextDescription());
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeExactUeiFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeExactUEIFilter("uei.opennms.org/bogus"));
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativePartialUEIFilter(){
        AlarmCriteria criteria = getCriteria(new NegativePartialUEIFilter("uei.opennms.org"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeServiceFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeServiceFilter(12, null));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNegativeSeverityFilter(){
        AlarmCriteria criteria = getCriteria(new NegativeSeverityFilter(OnmsSeverity.CRITICAL));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testNodeNameLikeFilter(){
        AlarmCriteria criteria = getCriteria(new NodeNameLikeFilter("mr"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testSeverityBetweenFilter(){
        AlarmCriteria criteria = getCriteria(new SeverityBetweenFilter(OnmsSeverity.CLEARED, OnmsSeverity.MAJOR));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testServiceFilter(){
        AlarmCriteria criteria = getCriteria(new ServiceFilter(1, null));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAfterFirstEventTime(){
        AlarmCriteria criteria = getCriteria(new AfterFirstEventTimeFilter(new Date()));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(0, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testDescriptionSubstringFilter(){
        AlarmCriteria criteria = getCriteria(new DescriptionSubstringFilter("alarm"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testLogMessageSubstringFilter(){
        AlarmCriteria criteria = getCriteria(new LogMessageSubstringFilter("this is a test"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }
    
    @Test
    @Transactional
    @JUnitTemporaryDatabase
    @SuppressWarnings("deprecation")
    public void testLogMessageMatchAnyFilter(){
        AlarmCriteria criteria = getCriteria(new LogMessageMatchesAnyFilter("log"));
        
        OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
        assertEquals(1, alarms.length);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    @SuppressWarnings("deprecation")
    public void testCategoryFilter() {
        // get all alarms
        final Set<OnmsAlarm> allAlarms = Arrays.stream(m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(new AlarmCriteria(new Filter[0])))).collect(Collectors.toSet());

        // create node to categories map
        final Map<Integer, Set<String>> nodeCategoriesMap = new TreeMap<>();

        // iterate over all nodes
        for (final OnmsNode onmsNode : m_nodeDao.findAll()) {
            // get node's categories
            nodeCategoriesMap.put(onmsNode.getId(), onmsNode.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));
        }

        // now iterate over all cetegories
        final List<OnmsCategory> onmsCategories = m_categoryDao.findAll();
        for (final OnmsCategory alarmFound : onmsCategories) {
            // get alarms for given category
            final AlarmCriteria criteria = getCriteria(new CategoryFilter(alarmFound.getName()));
            final OnmsAlarm[] alarmsByCategory = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
            // check that each alarm has a node with the given category associated
            for (final OnmsAlarm onmsAlarm : alarmsByCategory) {
                assertNotNull(onmsAlarm.getNodeId());
                assertTrue(nodeCategoriesMap.get(onmsAlarm.getNodeId()).contains(alarmFound.getName()));
                // remote from all alarms
                allAlarms.remove(onmsAlarm);
            }
        }

        // check remaining alarms
        for (final OnmsAlarm remainingAlarm : allAlarms) {
            // no node or no categories assigned
            assertTrue(remainingAlarm.getNodeId() == null || nodeCategoriesMap.get(remainingAlarm.getNodeId()).isEmpty());
        }
    }

    // testParmsLikeFilter and testParmsNotLikeFilter removed:
    // Event parameters are no longer stored in PostgreSQL (events table eliminated).


    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testSituations() {
        OnmsDistPoller poller = m_dbPopulator.getDistPollerDao().whoami();

        final Date eventTime = new Date();
        final String eventUei = "uei://org/opennms/test/EventDaoTest";
        final int eventSeverity = OnmsSeverity.CRITICAL.getId();

        final OnmsNode node = m_dbPopulator.getNodeDao().findAll().iterator().next();

        final OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(node);
        alarm1.setUei(eventUei);
        alarm1.setSeverityId(eventSeverity);
        alarm1.setFirstEventTime(eventTime);
        alarm1.setLastEventTime(eventTime);
        alarm1.setEventTsid(1L);
        alarm1.setEventUei(eventUei);
        alarm1.setCounter(1);
        alarm1.setDistPoller(poller);
        m_dbPopulator.getAlarmDao().save(alarm1);
        m_dbPopulator.getAlarmDao().flush();

        final OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setNode(node);
        alarm2.setUei(eventUei);
        alarm2.setSeverityId(eventSeverity);
        alarm2.setFirstEventTime(eventTime);
        alarm2.setLastEventTime(eventTime);
        alarm2.setEventTsid(1L);
        alarm2.setEventUei(eventUei);
        alarm2.setCounter(1);
        alarm2.setDistPoller(poller);
        alarm2.setRelatedAlarms(Sets.newHashSet(alarm1));
        m_dbPopulator.getAlarmDao().save(alarm2);
        m_dbPopulator.getAlarmDao().flush();

        assertEquals(3, m_dbPopulator.getAlarmDao().findAll().size());

        AlarmCriteria criteriaForSituations = new AlarmCriteria(new SituationFilter(true));
        AlarmCriteria criteriaForAlarms = new AlarmCriteria(new SituationFilter(false));

        final OnmsAlarm[] situations = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteriaForSituations));
        final OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteriaForAlarms));

        assertEquals(1, situations.length);
        assertEquals(2, alarms.length);
    }

    public void checkFilteringAndSorting(final SortStyle sortStyle, final List<Filter> filterList, final int limit, final int multiple, final int expectedCount, final int expectedResults) {
        final AlarmQueryParms parms = new AlarmQueryParms();
        parms.ackType = AcknowledgeType.UNACKNOWLEDGED;
        parms.display = null;
        parms.filters = filterList;
        parms.limit = limit;
        parms.multiple = multiple;
        // apply the sort style
        parms.sortStyle = sortStyle;

        final AlarmCriteria queryCriteria = new AlarmCriteria(parms);
        final AlarmCriteria countCriteria = new AlarmCriteria(filterList, AcknowledgeType.UNACKNOWLEDGED);

        // get alarm entities
        final OnmsAlarm[] alarms = m_daoAlarmRepo.getMatchingAlarms(AlarmUtil.getOnmsCriteria(queryCriteria));
        // get alarm count
        final long alarmCount = m_daoAlarmRepo.countMatchingAlarms(AlarmUtil.getOnmsCriteria(countCriteria));

        // check expected values
        assertEquals(expectedCount, alarmCount);
        assertEquals(expectedResults, alarms.length);
        // check that the entity is populated with values
        assertNotNull(alarms[0].getId());
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testFilteringAndSorting() {
        final OnmsDistPoller poller = m_dbPopulator.getDistPollerDao().whoami();

        // set up alarms...
        final Date eventTime = new Date();
        final String eventUei = "uei://org/opennms/test/EventDaoTest";
        final int eventSeverity = OnmsSeverity.CRITICAL.getId();

        final OnmsNode node = m_dbPopulator.getNodeDao().findAll().iterator().next();

        final OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setNode(node);
        alarm1.setUei(eventUei);
        alarm1.setSeverityId(eventSeverity);
        alarm1.setFirstEventTime(eventTime);
        alarm1.setLastEventTime(eventTime);
        alarm1.setEventTsid(1L);
        alarm1.setEventUei(eventUei);
        alarm1.setCounter(1);
        alarm1.setDistPoller(poller);
        m_dbPopulator.getAlarmDao().save(alarm1);
        m_dbPopulator.getAlarmDao().flush();

        final OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setNode(node);
        alarm2.setUei(eventUei);
        alarm2.setSeverityId(eventSeverity);
        alarm2.setFirstEventTime(eventTime);
        alarm2.setLastEventTime(eventTime);
        alarm2.setEventTsid(1L);
        alarm2.setEventUei(eventUei);
        alarm2.setCounter(1);
        alarm2.setDistPoller(poller);
        m_dbPopulator.getAlarmDao().save(alarm2);
        m_dbPopulator.getAlarmDao().flush();

        // ...and one situation
        final OnmsAlarm alarm3 = new OnmsAlarm();
        alarm3.setNode(node);
        alarm3.setUei(eventUei);
        alarm3.setSeverityId(eventSeverity);
        alarm3.setFirstEventTime(eventTime);
        alarm3.setLastEventTime(eventTime);
        alarm3.setEventTsid(1L);
        alarm3.setEventUei(eventUei);
        alarm3.setCounter(1);
        alarm3.setDistPoller(poller);
        alarm2.setRelatedAlarms(Sets.newHashSet(alarm1, alarm2));
        m_dbPopulator.getAlarmDao().save(alarm3);
        m_dbPopulator.getAlarmDao().flush();

        // some more alarms
        for(int i=0;i<10; i++) {
            final OnmsAlarm alarm = new OnmsAlarm();
            alarm.setNode(node);
            alarm.setUei(eventUei);
            alarm.setSeverityId(eventSeverity);
            alarm.setFirstEventTime(eventTime);
            alarm.setLastEventTime(eventTime);
            alarm.setEventTsid(1L);
            alarm.setEventUei(eventUei);
            alarm.setCounter(1);
            alarm.setDistPoller(poller);
            m_dbPopulator.getAlarmDao().save(alarm);
            m_dbPopulator.getAlarmDao().flush();
        }

        // 14 in total
        assertEquals(14, m_dbPopulator.getAlarmDao().findAll().size());

        // check for the single situation
        checkFilteringAndSorting(SortStyle.ID, Lists.newArrayList(new SituationFilter(true)), 10, 0, 1, 1);
        // check for all, first page of two
        checkFilteringAndSorting(SortStyle.ID, Lists.newArrayList(), 10, 0, 14, 10);
        // check for second page
        checkFilteringAndSorting(SortStyle.ID, Lists.newArrayList(), 10, 1, 14, 4);
        // check that all the sort styles work
        for(SortStyle sortStyle : SortStyle.values()) {
            checkFilteringAndSorting(sortStyle, Lists.newArrayList(new SituationFilter(true)), 10, 0, 1, 1);
        }
    }
    
    private AlarmCriteria getCriteria(Filter...filters){
        return new AlarmCriteria(filters);
    }
}
