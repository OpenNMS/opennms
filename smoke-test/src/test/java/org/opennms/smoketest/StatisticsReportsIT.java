/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

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

public class StatisticsReportsIT extends OpenNMSSeleniumTestCase {

    @Before
    public void setUp() throws Exception {
        m_driver.get(getBaseUrl() + "opennms/statisticsReports/index.htm");

    }

    @Test
    public void hasReportLinkThatMatchDescription() throws Exception {
        Date startOfTest = new Date();

        HibernateDaoFactory daoFactory = new HibernateDaoFactory(getPostgresService());
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
                .until(DaoUtils.findMatchingCallable(statisticsReportDao,
                        new CriteriaBuilder(StatisticsReport.class).ge("startDate", startOfTest).toCriteria()),
                        notNullValue());
        m_driver.navigate().refresh();

        assertNotNull(findElementByLink("Hourly Top 10 responses across all nodes"));
    }

}
