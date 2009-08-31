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
 * 2007 Apr 05: Created this file. - dj@opennms.org
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

import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.dao.castor.statsd.PackageReport;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ReportDefinition implements InitializingBean {
    private PackageReport m_report;
    private Class<? extends AttributeStatisticVisitorWithResults> m_reportClass;

    private Integer m_count;
    private String m_consolidationFunction;
    private RelativeTime m_relativeTime;
    private String m_resourceTypeMatch;
    private String m_attributeMatch;
    private String m_resourceAttributeKey;
    private String m_resourceAttributeValueMatch;
    
    public String getAttributeMatch() {
        return m_attributeMatch;
    }
    public void setAttributeMatch(String attributeMatch) {
        m_attributeMatch = attributeMatch;
    }
    public String getConsolidationFunction() {
        return m_consolidationFunction;
    }
    public void setConsolidationFunction(String consolidationFunction) {
        m_consolidationFunction = consolidationFunction;
    }
    public Integer getCount() {
        return m_count;
    }
    public void setCount(Integer count) {
        m_count = count;
    }
    public String getResourceTypeMatch() {
        return m_resourceTypeMatch;
    }
    public void setResourceTypeMatch(String resourceTypeMatch) {
        this.m_resourceTypeMatch = resourceTypeMatch;
    }
    public Class<? extends AttributeStatisticVisitorWithResults> getReportClass() {
        return m_reportClass;
    }
    public void setReportClass(Class<? extends AttributeStatisticVisitorWithResults> reportClass) {
        /*
         * Even though we are using generics, it's just a compile-time check,
         * so let's do a runtime check, too.
         */
        Assert.isAssignable(AttributeStatisticVisitorWithResults.class, reportClass, "the value of property reportClass does not implement the interface " + AttributeStatisticVisitorWithResults.class.getName() + "; ");

        m_reportClass = reportClass;
    }
    public RelativeTime getRelativeTime() {
        return m_relativeTime;
    }
    public void setRelativeTime(RelativeTime relativeTime) {
        m_relativeTime = relativeTime;
    }
    public PackageReport getReport() {
        return m_report;
    }
    public void setReport(PackageReport report) {
        m_report = report;
    }
    public void setResourceAttributeKey(String resourceAttributeKey) {
        m_resourceAttributeKey = resourceAttributeKey;
    }
    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch) {
        m_resourceAttributeValueMatch = resourceAttributeValueMatch;
    }
    public String getResourceAttributeKey() {
        return m_resourceAttributeValueMatch;
    }
    public String getResourceAttributeValueMatch() {
        return m_resourceAttributeKey;
    }
    
    
    public String getCronExpression() {
        return getReport().getSchedule();
    }
    public String getDescription() {
        return getReport().getDescription();
    }
    public String getName() {
        return getReport().getReport().getName();
    }
    public Long getRetainInterval() {
        return getReport().getRetainInterval();
    }

    
    public ReportInstance createReport(ResourceDao resourceDao, RrdDao rrdDao, FilterDao filterDao) throws Exception {
        Assert.notNull(resourceDao, "resourceDao argument must not be null");
        Assert.notNull(rrdDao, "rrdDao argument must not be null");
        Assert.notNull(filterDao, "filterDao argument must not be null");
        
        AttributeStatisticVisitorWithResults visitor;
        try {
            visitor = getReportClass().newInstance();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not instantiate visitor object; nested exception: " + e, e);
        }

        ReportInstance report;
        if (getReport().getPackage().getFilter() != null) {
            FilteredReportInstance thisReport = new FilteredReportInstance(visitor);
            thisReport.setResourceDao(resourceDao);
            thisReport.setRrdDao(rrdDao);
            thisReport.setFilterDao(filterDao);
            thisReport.setFilter(getReport().getPackage().getFilter());
            
            report = thisReport;
        } else {
            UnfilteredReportInstance thisReport = new UnfilteredReportInstance(visitor); 
            thisReport.setResourceDao(resourceDao);
            thisReport.setRrdDao(rrdDao);
            
            report = thisReport;
        }
        
        report.setReportDefinition(this);
        
        report.setStartTime(getRelativeTime().getStart().getTime());
        report.setEndTime(getRelativeTime().getEnd().getTime());
        
        report.setCount(getCount());
        report.setConsolidationFunction(getConsolidationFunction());
        report.setResourceTypeMatch(getResourceTypeMatch());
        report.setAttributeMatch(getAttributeMatch());

        report.setResourceAttributeKey(m_resourceAttributeKey);
        report.setResourceAttributeValueMatch(m_resourceAttributeValueMatch);
        
        if (report instanceof InitializingBean) {
            ((InitializingBean) report).afterPropertiesSet();
        }
        
        return report;
    }
    
    @Override
    public String toString() {
        return m_report.getDescription();
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_report != null, "property report must be set to a non-null value");
        Assert.state(m_count != null, "property count must be set to a non-null value");
        Assert.state(m_consolidationFunction != null, "property consolidationFunction must be set to a non-null value");
        Assert.state(m_relativeTime != null, "property relativeTime must be set to a non-null value");
        Assert.state(m_resourceTypeMatch != null, "property resourceTypeMatch must be set to a non-null value");
        Assert.state(m_attributeMatch != null, "property attributeMatch must be set to a non-null value");
        Assert.state(m_reportClass != null, "property reportClass must be set to a non-null value");
    }
}
