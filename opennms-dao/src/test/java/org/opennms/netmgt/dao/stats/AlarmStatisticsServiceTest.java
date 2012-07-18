package org.opennms.netmgt.dao.stats;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
//        "classpath*:/META-INF/opennms/component-service.xml",
//        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
//        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AlarmStatisticsServiceTest implements InitializingBean {
    @Autowired
    DatabasePopulator m_dbPopulator;

    @Autowired
    private DefaultAlarmStatisticsService m_statisticsService;

    private static boolean m_initialized = false;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        if (!m_initialized) m_dbPopulator.populateDatabase();
        m_initialized = true;
    }
    
    @Test
    public void testCount() {
    	final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);

    	cb.fetch("firstEvent", FetchType.EAGER);
    	cb.fetch("lastEvent", FetchType.EAGER);

        cb.alias("node", "node", JoinType.LEFT_JOIN);
        cb.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        cb.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);

        cb.distinct();

        final int count = m_statisticsService.getTotalCount(cb.toCriteria());
        assertEquals(1, count);
    }

    @Test
    public void testCountBySeverity() {
    	final CriteriaBuilder cb = new CriteriaBuilder(OnmsAlarm.class);
    	cb.ge("severity", OnmsSeverity.NORMAL);

    	cb.fetch("firstEvent", FetchType.EAGER);
    	cb.fetch("lastEvent", FetchType.EAGER);

        cb.alias("node", "node", JoinType.LEFT_JOIN);
        cb.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        cb.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);

        cb.distinct();

        final int count = m_statisticsService.getTotalCount(cb.toCriteria());
        assertEquals(1, count);
    }
}
