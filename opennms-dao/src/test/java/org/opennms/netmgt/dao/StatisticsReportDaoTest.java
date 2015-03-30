/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class StatisticsReportDaoTest implements InitializingBean {
	@Autowired
    private StatisticsReportDao m_statisticsReportDao;
	
	@Autowired
    private ResourceReferenceDao m_resourceReferenceDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
