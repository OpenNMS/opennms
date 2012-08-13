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

import java.util.Date;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * <p>Abstract AbstractReportInstance class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public abstract class AbstractReportInstance implements ReportInstance, InitializingBean {

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
    @Override
    public Date getJobCompletedDate() {
        return m_jobCompletedDate;
    }

    /**
     * <p>getJobStartedDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getJobStartedDate() {
        return m_jobStartedDate;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return getReportDefinition().getName();
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDescription() {
        return getReportDefinition().getDescription();
    }

    /**
     * <p>getRetainInterval</p>
     *
     * @return a long.
     */
    @Override
    public long getRetainInterval() {
        return getReportDefinition().getRetainInterval();
    }

    /**
     * <p>getReportDefinition</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    @Override
    public ReportDefinition getReportDefinition() {
        return m_reportDefinition;
    }

    /**
     * <p>setReportDefinition</p>
     *
     * @param reportDefinition a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     */
    @Override
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
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_reportDefinition != null, "property reportDefinition must be set to a non-null value");
    }

}
