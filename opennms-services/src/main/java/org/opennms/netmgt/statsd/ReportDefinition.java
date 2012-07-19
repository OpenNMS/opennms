/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.dao.castor.statsd.PackageReport;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.model.AttributeStatisticVisitorWithResults;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>ReportDefinition class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
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
    
    /**
     * <p>getAttributeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAttributeMatch() {
        return m_attributeMatch;
    }
    /**
     * <p>setAttributeMatch</p>
     *
     * @param attributeMatch a {@link java.lang.String} object.
     */
    public void setAttributeMatch(String attributeMatch) {
        m_attributeMatch = attributeMatch;
    }
    /**
     * <p>getConsolidationFunction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConsolidationFunction() {
        return m_consolidationFunction;
    }
    /**
     * <p>setConsolidationFunction</p>
     *
     * @param consolidationFunction a {@link java.lang.String} object.
     */
    public void setConsolidationFunction(String consolidationFunction) {
        m_consolidationFunction = consolidationFunction;
    }
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getCount() {
        return m_count;
    }
    /**
     * <p>setCount</p>
     *
     * @param count a {@link java.lang.Integer} object.
     */
    public void setCount(Integer count) {
        m_count = count;
    }
    /**
     * <p>getResourceTypeMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeMatch() {
        return m_resourceTypeMatch;
    }
    /**
     * <p>setResourceTypeMatch</p>
     *
     * @param resourceTypeMatch a {@link java.lang.String} object.
     */
    public void setResourceTypeMatch(String resourceTypeMatch) {
        this.m_resourceTypeMatch = resourceTypeMatch;
    }
    /**
     * <p>getReportClass</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<? extends AttributeStatisticVisitorWithResults> getReportClass() {
        return m_reportClass;
    }
    /**
     * <p>setReportClass</p>
     *
     * @param reportClass a {@link java.lang.Class} object.
     */
    public void setReportClass(Class<? extends AttributeStatisticVisitorWithResults> reportClass) {
        /*
         * Even though we are using generics, it's just a compile-time check,
         * so let's do a runtime check, too.
         */
        Assert.isAssignable(AttributeStatisticVisitorWithResults.class, reportClass, "the value of property reportClass does not implement the interface " + AttributeStatisticVisitorWithResults.class.getName() + "; ");

        m_reportClass = reportClass;
    }
    /**
     * <p>getRelativeTime</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.RelativeTime} object.
     */
    public RelativeTime getRelativeTime() {
        return m_relativeTime;
    }
    /**
     * <p>setRelativeTime</p>
     *
     * @param relativeTime a {@link org.opennms.netmgt.statsd.RelativeTime} object.
     */
    public void setRelativeTime(RelativeTime relativeTime) {
        m_relativeTime = relativeTime;
    }
    /**
     * <p>getReport</p>
     *
     * @return a {@link org.opennms.netmgt.dao.castor.statsd.PackageReport} object.
     */
    public PackageReport getReport() {
        return m_report;
    }
    /**
     * <p>setReport</p>
     *
     * @param report a {@link org.opennms.netmgt.dao.castor.statsd.PackageReport} object.
     */
    public void setReport(PackageReport report) {
        m_report = report;
    }
    /**
     * <p>setResourceAttributeKey</p>
     *
     * @param resourceAttributeKey a {@link java.lang.String} object.
     */
    public void setResourceAttributeKey(String resourceAttributeKey) {
        m_resourceAttributeKey = resourceAttributeKey;
    }
    /**
     * <p>setResourceAttributeValueMatch</p>
     *
     * @param resourceAttributeValueMatch a {@link java.lang.String} object.
     */
    public void setResourceAttributeValueMatch(String resourceAttributeValueMatch) {
        m_resourceAttributeValueMatch = resourceAttributeValueMatch;
    }
    /**
     * <p>getResourceAttributeKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeKey() {
        return m_resourceAttributeValueMatch;
    }
    /**
     * <p>getResourceAttributeValueMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceAttributeValueMatch() {
        return m_resourceAttributeKey;
    }
    
    
    /**
     * <p>getCronExpression</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCronExpression() {
        return getReport().getSchedule();
    }
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return getReport().getDescription();
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return getReport().getReport().getName();
    }
    /**
     * <p>getRetainInterval</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getRetainInterval() {
        return getReport().getRetainInterval();
    }

    
    /**
     * <p>createReport</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     * @param rrdDao a {@link org.opennms.netmgt.dao.RrdDao} object.
     * @param filterDao a {@link org.opennms.netmgt.filter.FilterDao} object.
     * @return a {@link org.opennms.netmgt.statsd.ReportInstance} object.
     * @throws java.lang.Exception if any.
     */
    public ReportInstance createReport(NodeDao nodeDao, ResourceDao resourceDao, RrdDao rrdDao, FilterDao filterDao) throws Exception {
        Assert.notNull(resourceDao, "resourceDao argument must not be null");
        Assert.notNull(rrdDao, "rrdDao argument must not be null");
        Assert.notNull(filterDao, "filterDao argument must not be null");
        
        AttributeStatisticVisitorWithResults visitor;
        try {
            visitor = getReportClass().newInstance();
        } catch (Throwable e) {
            throw new DataAccessResourceFailureException("Could not instantiate visitor object; nested exception: " + e, e);
        }

        ReportInstance report;
        if (getReport().getPackage().getFilter() != null) {
            FilteredReportInstance thisReport = new FilteredReportInstance(visitor);
            thisReport.setNodeDao(nodeDao);
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
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_report.getDescription();
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
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
