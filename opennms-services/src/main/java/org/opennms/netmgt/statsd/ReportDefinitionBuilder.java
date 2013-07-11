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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.config.statsd.model.PackageReport;
import org.opennms.netmgt.config.statsd.model.Report;
import org.opennms.netmgt.config.statsd.model.StatsdPackage;
import org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>ReportDefinitionBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportDefinitionBuilder implements InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReportDefinitionBuilder.class);
    
    private StatisticsDaemonConfigDao m_statsdConfigDao;
    
    /**
     * <p>reload</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    public void reload() throws DataAccessResourceFailureException {
        m_statsdConfigDao.reloadConfiguration();
        
    }

    /**
     * Builds and schedules all reports enabled in the statsd-configuration.
     * This method has the capability to throw a ton of exceptions, just generically throwing <code>Exception</code>
     *
     * @return a <code>Collection</code> of enabled reports from the statsd-configuration.
     * @throws java.lang.Exception if any.
     */
    public Collection<ReportDefinition> buildReportDefinitions() throws Exception {
        Set<ReportDefinition> reportDefinitions = new HashSet<ReportDefinition>();
        
        for (StatsdPackage pkg : m_statsdConfigDao.getPackages()) {
            for (PackageReport packageReport : pkg.getReports()) {
                Report report = packageReport.getReport();

                if (!packageReport.isEnabled()) {
                    LOG.debug("skipping report '{}' in package '{}' because the report is not enabled", report.getName(), pkg.getName());
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
                
                BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(reportDef);
                try {
                    bw.setPropertyValues(packageReport.getAggregateParameters());
                } catch (BeansException e) {
                    LOG.error("Could not set properties on report definition: {}", e.getMessage(), e);
                }
                
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

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_statsdConfigDao != null, "property statsdConfigDao must be set to a non-null value");
    }

    /**
     * <p>getStatsdConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao} object.
     */
    public StatisticsDaemonConfigDao getStatsdConfigDao() {
        return m_statsdConfigDao;
    }

    /**
     * <p>setStatsdConfigDao</p>
     *
     * @param statsdConfigDao a {@link org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao} object.
     */
    public void setStatsdConfigDao(StatisticsDaemonConfigDao statsdConfigDao) {
        m_statsdConfigDao = statsdConfigDao;
    }
}
