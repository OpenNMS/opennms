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

package org.opennms.web.svclayer.support;

import java.util.List;
import java.util.Set;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.StatisticsReportDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.web.command.StatisticsReportCommand;
import org.opennms.web.svclayer.StatisticsReportService;
import org.opennms.web.svclayer.support.StatisticsReportModel.Datum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;

/**
 * Web service layer implementation for statistics reports.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultStatisticsReportService implements StatisticsReportService, InitializingBean {
    private StatisticsReportDao m_statisticsReportDao;
    private ResourceDao m_resourceDao;

    /**
     * <p>getStatisticsReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<StatisticsReport> getStatisticsReports() {
        return m_statisticsReportDao.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public StatisticsReportModel getReport(StatisticsReportCommand command, BindException errors) {
        StatisticsReportModel model = new StatisticsReportModel();
        model.setErrors(errors);
        
        if (errors.hasErrors()) {
            return model;
        }
        
        Assert.notNull(command.getId(), "id property on command object cannot be null");
        
        StatisticsReport report = m_statisticsReportDao.load(command.getId());
        model.setReport(report);
        
        m_statisticsReportDao.initialize(report);
        final Set<StatisticsReportData> data = report.getData();
        m_statisticsReportDao.initialize(data);
        
        for (StatisticsReportData reportDatum : data) {
            Datum d = new Datum();
            d.setValue(reportDatum.getValue());
            OnmsResource resource = m_resourceDao.getResourceById(reportDatum.getResourceId());
            if (resource == null) {
                ThreadCategory.getInstance(getClass()).warn("Could not find resource for statistics report: " + reportDatum.getResourceId());
            } else {
                d.setResource(resource);
            }
            model.addData(d);
        }
        
        return model;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_statisticsReportDao != null, "property statisticsReportDao must be set to a non-null value");
        Assert.state(m_resourceDao != null, "property resourceDao must be set to a non-null value");
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
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }
}
