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
package org.opennms.smoketest;

import static org.awaitility.Awaitility.await;
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

public class StatisticsReportsIT extends OpenNMSSeleniumIT {

    @Before
    public void setUp() throws Exception {
        driver.get(getBaseUrlInternal() + "opennms/statisticsReports/index.htm");
    }

    @Test
    public void hasReportLinkThatMatchDescription() throws Exception {
        Date startOfTest = new Date();

        HibernateDaoFactory daoFactory = stack.postgres().getDaoFactory();
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
        driver.navigate().refresh();

        assertNotNull(findElementByLink("Hourly Top 10 responses across all nodes"));
    }

}
