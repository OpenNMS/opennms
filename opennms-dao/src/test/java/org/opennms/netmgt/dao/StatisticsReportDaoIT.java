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
package org.opennms.netmgt.dao;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.ResourceReferenceDao;
import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


/**
 * Unit tests for StatisticsReportDao
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatisticsReportDao
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class StatisticsReportDaoIT implements InitializingBean {
	@Autowired
    private StatisticsReportDao m_statisticsReportDao;
	
	@Autowired
    private ResourceReferenceDao m_resourceReferenceDao;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

	@Test
	@Transactional
    public void testSave() throws Exception {
        StatisticsReport report = new StatisticsReport();
        report.setName("A Mighty Fine Report");
        report.setDescription("hello!");
        report.setStartDate(new Date());
        report.setEndDate(new Date());
        report.setJobStartedDate(new Date());
        report.setJobCompletedDate(new Date());
        report.setPurgeDate(new Date());
        
		{
            ResourceReference resource = new ResourceReference();
            resource.setResourceId("foo");
            m_resourceReferenceDao.save(resource);

            StatisticsReportData data = new StatisticsReportData();
            data.setReport(report);
            data.setResource(resource);
            data.setValue(0.0);
            report.addData(data);
        }
        

        {
            ResourceReference resource = new ResourceReference();
            resource.setResourceId("bar");
            m_resourceReferenceDao.save(resource);
            

            StatisticsReportData data = new StatisticsReportData();
            data.setReport(report);
            data.setResource(resource);
            data.setValue(0.0);
            report.addData(data);
        }
        
        m_statisticsReportDao.save(report);
    }
}
