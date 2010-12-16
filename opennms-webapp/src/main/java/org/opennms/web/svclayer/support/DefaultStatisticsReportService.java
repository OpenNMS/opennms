/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Oct 22: Use new ResourceDao method names. - dj@opennms.org
 * 2007 Apr 10: Created this file. - dj@opennms.org
 * 
 * Created: April 10, 2007
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
package org.opennms.web.svclayer.support;

import java.util.List;

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
    public List<StatisticsReport> getStatisticsReports() {
        return m_statisticsReportDao.findAll();
    }

    /** {@inheritDoc} */
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
        m_statisticsReportDao.initialize(report.getData());
        
        for (StatisticsReportData datum : report.getData()) {
            Datum d = new Datum();
            d.setValue(datum.getValue());
            OnmsResource resource = m_resourceDao.getResourceById(datum.getResourceId());
            if (resource == null) {
                ThreadCategory.getInstance(getClass()).warn("Could not find resource for statistics report: " + datum.getResourceId());
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
