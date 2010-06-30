/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: April 14, 2007
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

import java.util.Date;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * <p>Abstract AbstractReportInstance class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public abstract class AbstractReportInstance implements InitializingBean {

    private ReportDefinition m_reportDefinition;
    private Date m_jobCompletedDate;
    private Date m_jobStartedDate;

    /**
     * <p>Constructor for AbstractReportInstance.</p>
     */
    public AbstractReportInstance() {
        super();
    }

    /**
     * <p>getJobCompletedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getJobCompletedDate() {
        return m_jobCompletedDate;
    }

    /**
     * <p>getJobStartedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getJobStartedDate() {
        return m_jobStartedDate;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return getReportDefinition().getName();
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return getReportDefinition().getDescription();
    }

    /**
     * <p>getRetainInterval</p>
     *
     * @return a long.
     */
    public long getRetainInterval() {
        return getReportDefinition().getRetainInterval();
    }

    /**
     * <p>getReportDefinition</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    public ReportDefinition getReportDefinition() {
        return m_reportDefinition;
    }

    /**
     * <p>setReportDefinition</p>
     *
     * @param reportDefinition a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    public void setReportDefinition(ReportDefinition reportDefinition) {
        m_reportDefinition = reportDefinition;
    }

    /**
     * <p>setJobCompletedDate</p>
     *
     * @param jobCompletedDate a {@link java.util.Date} object.
     */
    public void setJobCompletedDate(Date jobCompletedDate) {
        m_jobCompletedDate = jobCompletedDate;
    }

    /**
     * <p>setJobStartedDate</p>
     *
     * @param jobStartedDate a {@link java.util.Date} object.
     */
    public void setJobStartedDate(Date jobStartedDate) {
        m_jobStartedDate = jobStartedDate;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#afterPropertiesSet()
     */
    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.state(m_reportDefinition != null, "property reportDefinition must be set to a non-null value");
    }

}
