/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.statsd;

import java.util.Date;

import org.opennms.netmgt.dao.ResourceReferenceDao;
import org.opennms.netmgt.dao.StatisticsReportDao;
import org.opennms.netmgt.model.AttributeStatistic;
import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DatabaseReportPersister class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DatabaseReportPersister implements ReportPersister, InitializingBean {
    private StatisticsReportDao m_statisticsReportDao;
    private ResourceReferenceDao m_resourceReferenceDao;

    /** {@inheritDoc} */
    @Override
    public void persist(ReportInstance report) {
        StatisticsReport dbReport = new StatisticsReport();
        dbReport.setName(report.getName());
        dbReport.setDescription(report.getDescription());
        dbReport.setStartDate(new Date(report.getStartTime()));
        dbReport.setEndDate(new Date(report.getEndTime()));
        dbReport.setJobStartedDate(report.getJobStartedDate());
        dbReport.setJobCompletedDate(report.getJobCompletedDate());
        dbReport.setPurgeDate(new Date(report.getJobCompletedDate().getTime() + report.getRetainInterval()));

        for (AttributeStatistic stat : report.getResults()) {
            ResourceReference resource = getResourceReference(stat.getAttribute().getResource().getId());

            StatisticsReportData data = new StatisticsReportData();
            data.setResource(resource);
            data.setReport(dbReport);
            data.setValue(stat.getStatistic());
            dbReport.addData(data);
        }
        
        m_statisticsReportDao.save(dbReport);
    }

    private ResourceReference getResourceReference(String id) {
        ResourceReference resource = m_resourceReferenceDao.getByResourceId(id);
        if (resource != null) {
            return resource;
        }
      
        resource = new ResourceReference();
        resource.setResourceId(id);
        m_resourceReferenceDao.save(resource);

        return resource;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_statisticsReportDao != null, "property statisticsReportDao must be set to a non-null value");
        Assert.state(m_resourceReferenceDao != null, "property resourceReferenceDao must be set to a non-null value");
    }
    
    /**
     * <p>getStatisticsReportDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.StatisticsReportDao} object.
     */
    public StatisticsReportDao getStatisticsReportDao() {
        return m_statisticsReportDao;
    }

    /**
     * <p>setStatisticsReportDao</p>
     *
     * @param statisticsReportDao a {@link org.opennms.netmgt.dao.StatisticsReportDao} object.
     */
    public void setStatisticsReportDao(StatisticsReportDao statisticsReportDao) {
        m_statisticsReportDao = statisticsReportDao;
    }

    /**
     * <p>getResourceReferenceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ResourceReferenceDao} object.
     */
    public ResourceReferenceDao getResourceReferenceDao() {
        return m_resourceReferenceDao;
    }

    /**
     * <p>setResourceReferenceDao</p>
     *
     * @param resourceReferenceDao a {@link org.opennms.netmgt.dao.ResourceReferenceDao} object.
     */
    public void setResourceReferenceDao(ResourceReferenceDao resourceReferenceDao) {
        m_resourceReferenceDao = resourceReferenceDao;
    }
}
