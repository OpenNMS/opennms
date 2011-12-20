package org.opennms.netmgt.dao.stats;

import static org.junit.Assert.assertEquals;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
//        "classpath*:/META-INF/opennms/component-service.xml",
//        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
//        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AlarmStatisticsServiceTest {
    @Autowired
    DatabasePopulator m_dbPopulator;

    @Autowired
    private AlarmStatisticsService m_statisticsService;

    private static boolean m_initialized = false;
    
    @Before
    public void setUp() {
        Assert.notNull(m_statisticsService);

        MockLogAppender.setupLogging();
        if (!m_initialized) m_dbPopulator.populateDatabase();
        m_initialized = true;
    }
    
    @Test
    public void testCount() {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);

        criteria.setFetchMode("firstEvent", FetchMode.JOIN);
        criteria.setFetchMode("lastEvent", FetchMode.JOIN);
        
        criteria.createAlias("node", "node", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);

        criteria.setProjection(
                               Projections.distinct(
                                   Projections.projectionList().add(
                                       Projections.alias( Projections.property("id"), "id" )
                                   )
                               )
                           );

        OnmsCriteria rootCriteria = new OnmsCriteria(OnmsAlarm.class);
        rootCriteria.add(Subqueries.propertyIn("id", criteria.getDetachedCriteria()));
        // rootCriteria.addOrder(Order.desc("lastEventTime"));
        // rootCriteria.setMaxResults(1);
        // rootCriteria.setFirstResult(0);

        final int count = m_statisticsService.getTotalCount(rootCriteria);
        assertEquals(1, count);
    }

    @Test
    public void testCountBySeverity() {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);

        criteria.add(Restrictions.ge("severity", OnmsSeverity.NORMAL));
        
        criteria.setFetchMode("firstEvent", FetchMode.JOIN);
        criteria.setFetchMode("lastEvent", FetchMode.JOIN);
        
        criteria.createAlias("node", "node", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("node.ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);

        criteria.setProjection(
                               Projections.distinct(
                                   Projections.projectionList().add(
                                       Projections.alias( Projections.property("id"), "id" )
                                   )
                               )
                           );

        OnmsCriteria rootCriteria = new OnmsCriteria(OnmsAlarm.class);
        rootCriteria.add(Subqueries.propertyIn("id", criteria.getDetachedCriteria()));
        // rootCriteria.addOrder(Order.desc("lastEventTime"));
        // rootCriteria.setMaxResults(1);
        // rootCriteria.setFirstResult(0);

        final int count = m_statisticsService.getTotalCount(rootCriteria);
        assertEquals(1, count);
    }
}
