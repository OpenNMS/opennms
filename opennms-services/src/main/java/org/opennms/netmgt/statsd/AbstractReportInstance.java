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
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class AbstractReportInstance implements InitializingBean {

    private ReportDefinition m_reportDefinition;
    private Date m_jobCompletedDate;
    private Date m_jobStartedDate;

    public AbstractReportInstance() {
        super();
    }

    public Date getJobCompletedDate() {
        return m_jobCompletedDate;
    }

    public Date getJobStartedDate() {
        return m_jobStartedDate;
    }

    public String getName() {
        return getReportDefinition().getName();
    }

    public String getDescription() {
        return getReportDefinition().getDescription();
    }

    public long getRetainInterval() {
        return getReportDefinition().getRetainInterval();
    }

    public ReportDefinition getReportDefinition() {
        return m_reportDefinition;
    }

    public void setReportDefinition(ReportDefinition reportDefinition) {
        m_reportDefinition = reportDefinition;
    }

    public void setJobCompletedDate(Date jobCompletedDate) {
        m_jobCompletedDate = jobCompletedDate;
    }

    public void setJobStartedDate(Date jobStartedDate) {
        m_jobStartedDate = jobStartedDate;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.topn.Report#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        Assert.state(m_reportDefinition != null, "property reportDefinition must be set to a non-null value");
    }

}