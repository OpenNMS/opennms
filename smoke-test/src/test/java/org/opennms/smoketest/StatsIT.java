package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ResourceReferenceDao;
import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.netmgt.dao.hibernate.ResourceReferenceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.StatisticsReportDaoHibernate;
import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;

public class StatsIT extends OpenNMSSeleniumTestCase {
    
    @Before
    public void setUp() throws Exception {
        m_driver.get(getBaseUrl() + "opennms/statisticsReports/index.htm");
    }

    @Test
    public void hasReportLinkThatMatchDescription() throws Exception {
        
        Date startOfTest = new Date();
        InetSocketAddress pgsql = getTestEnvironment().getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        ResourceReferenceDao resourceReferenceDao = daoFactory.getDao(ResourceReferenceDaoHibernate.class);
        StatisticsReportDao statisticsReportDao = daoFactory.getDao(StatisticsReportDaoHibernate.class);

        StatisticsReport report = new StatisticsReport();
        report.setName("Top10_Response_Hourly");
        report.setDescription("Hourly Top 10 responses across all nodes");
        report.setStartDate(new Date());
        report.setEndDate(new Date());
        report.setJobStartedDate(new Date());
        report.setJobCompletedDate(new Date());
        report.setPurgeDate(new Date());

        ResourceReference resource = new ResourceReference();
        resource.setResourceId("node1");
        resourceReferenceDao.save(resource);

        StatisticsReportData data = new StatisticsReportData();
        data.setReport(report);
        data.setResource(resource);
        data.setValue(4.0);
        report.addData(data);

        statisticsReportDao.save(report);
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
        .until(DaoUtils.findMatchingCallable(statisticsReportDao, new CriteriaBuilder(StatisticsReport.class)
                .ge("createTime", startOfTest).toCriteria()), notNullValue());

        assertNotNull(findElementByLink("Hourly Top 10 responses across all nodes"));
    }

}
