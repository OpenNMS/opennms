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
 * 2007 Apr 10: Created this file. - dj@opennms.org
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
package org.opennms.netmgt.statsd;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.StatisticsDaemonConfigDao;
import org.opennms.netmgt.dao.castor.statsd.PackageReport;
import org.opennms.netmgt.dao.castor.statsd.Report;
import org.opennms.netmgt.dao.castor.statsd.StatsdPackage;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class ReportDefinitionBuilder implements InitializingBean {
    private StatisticsDaemonConfigDao m_statsdConfigDao;
    
    public void reload() throws DataAccessResourceFailureException {
        m_statsdConfigDao.reloadConfiguration();
        
    }

    /**
     * Builds and schedules all reports enabled in the statsd-configuration.
     * This method has the capability to throw a ton of exceptions, just generically throwing <code>Exception</code>
     * 
     * @return a <code>Collection</code> of enabled reports from the statsd-configuration.
     * @throws Exception
     */
    public Collection<ReportDefinition> buildReportDefinitions() throws Exception {
        Set<ReportDefinition> reportDefinitions = new HashSet<ReportDefinition>();
        
        for (StatsdPackage pkg : m_statsdConfigDao.getPackages()) {
            for (PackageReport packageReport : pkg.getReports()) {
                Report report = packageReport.getReport();

                if (!packageReport.isEnabled()) {
                    log().debug("skipping report '" + report.getName() + "' in package '" + pkg.getName() + "' because the report is not enabled");
                }
                
                Class<? extends AttributeStatisticVisitorWithResults> clazz;
                try {
                    clazz = createClassForReport(report);
                } catch (ClassNotFoundException e) {
                    throw new DataAccessResourceFailureException("Could not find class '" + report.getClassName() + "'; nested exception: " + e, e);
                }
                
                Assert.isAssignable(AttributeStatisticVisitorWithResults.class, clazz, "the class specified by class-name in the '" + report.getName() + "' report does not implement the interface " + AttributeStatisticVisitorWithResults.class.getName() + "; ");
                
                ReportDefinition reportDef = new ReportDefinition();
                reportDef.setReport(packageReport);
                reportDef.setReportClass(clazz);
                
                BeanWrapper bw = new BeanWrapperImpl(reportDef);
                bw.setPropertyValues(packageReport.getAggregateParameters());
                
                reportDef.afterPropertiesSet();

                reportDefinitions.add(reportDef);
            }
        }
        
        return reportDefinitions;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AttributeStatisticVisitorWithResults> createClassForReport(Report report) throws ClassNotFoundException {
        return (Class<? extends AttributeStatisticVisitorWithResults>) Class.forName(report.getClassName());
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() {
        Assert.state(m_statsdConfigDao != null, "property statsdConfigDao must be set to a non-null value");
    }

    public StatisticsDaemonConfigDao getStatsdConfigDao() {
        return m_statsdConfigDao;
    }

    public void setStatsdConfigDao(StatisticsDaemonConfigDao statsdConfigDao) {
        m_statsdConfigDao = statsdConfigDao;
    }
}
