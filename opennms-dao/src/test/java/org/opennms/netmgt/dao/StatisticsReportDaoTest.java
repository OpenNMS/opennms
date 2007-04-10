/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;


/**
 * Unit tests for StatisticsReportDao
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatisticsReportDao
 */
public class StatisticsReportDaoTest extends AbstractTransactionalDaoTestCase {
    private StatisticsReportDao m_statisticsReportDao;
    private StatisticsReportDataDao m_statisticsReportDataDao;
    private ResourceReferenceDao m_resourceReferenceDao;
    
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
        
        setComplete();
        endTransaction();
    }
    
    public StatisticsReportDao getStatisticsReportDao() {
        return m_statisticsReportDao;
    }

    public void setStatisticsReportDao(StatisticsReportDao statisticsReportDao) {
        m_statisticsReportDao = statisticsReportDao;
    }


    public StatisticsReportDataDao getStatisticsReportDataDao() {
        return m_statisticsReportDataDao;
    }


    public void setStatisticsReportDataDao(StatisticsReportDataDao statisticsReportDataDao) {
        m_statisticsReportDataDao = statisticsReportDataDao;
    }


    public ResourceReferenceDao getResourceReferenceDao() {
        return m_resourceReferenceDao;
    }


    public void setResourceReferenceDao(ResourceReferenceDao resourceReferenceDao) {
        m_resourceReferenceDao = resourceReferenceDao;
    }
}
