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

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ReportDefinition implements InitializingBean {
    private String m_name;
    private Integer m_count;
    private String m_consolidationFunction;
    private RelativeTime m_relativeTime;
    private String m_resourceTypeMatch;
    private String m_attributeMatch;
    private Class m_reportClass;
    private String m_cronExpression;
    
    public String getCronExpression() {
        return m_cronExpression;
    }
    public void setCronExpression(String cronExpression) {
        m_cronExpression = cronExpression;
    }
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
    public Class getReportClass() {
        return m_reportClass;
    }
    public void setReportClass(Class reportClass) {
        m_reportClass = reportClass;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public RelativeTime getRelativeTime() {
        return m_relativeTime;
    }
    public void setRelativeTime(RelativeTime relativeTime) {
        m_relativeTime = relativeTime;
    }
    
    public ReportInstance createReport(ResourceDao resourceDao, RrdDao rrdDao) {
        ReportInstance report;
        try {
            report = (ReportInstance) m_reportClass.newInstance();
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Could not instantiate report object; nested exception: " + e, e);
        }
        
        report.setName(m_name);
        report.setResourceDao(resourceDao);
        report.setRrdDao(rrdDao);
        
        report.setStartTime(getRelativeTime().getStart().getTime());
        report.setEndTime(getRelativeTime().getEnd().getTime());
        
        report.setCount(getCount());
        report.setConsolidationFunction(getConsolidationFunction());
        report.setResourceTypeMatch(getResourceTypeMatch());
        report.setAttributeMatch(getAttributeMatch());
        
        report.afterPropertiesSet();
        
        return report;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_name != null, "property name must be set to a non-null value");
        Assert.state(m_count != null, "property count must be set to a non-null value");
        Assert.state(m_consolidationFunction != null, "property consolidationFunction must be set to a non-null value");
        Assert.state(m_relativeTime != null, "property relativeTime must be set to a non-null value");
        Assert.state(m_resourceTypeMatch != null, "property resourceTypeMatch must be set to a non-null value");
        Assert.state(m_attributeMatch != null, "property attributeMatch must be set to a non-null value");
        Assert.state(m_reportClass != null, "property reportClass must be set to a non-null value");
        Assert.state(m_cronExpression != null, "property cronExpression must be set to a non-null value");
    }
}
