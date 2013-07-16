/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.reporting.service;

import org.opennms.netmgt.config.reportd.Report;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>ReportJob class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportJob implements Job {

    
    /** Constant <code>KEY="report"</code> */
    protected static final String KEY = "report";

    private Reportd m_reportd;

    
    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
            m_reportd.runReport((Report)context.getJobDetail().getJobDataMap().get(KEY));
    }


    /**
     * <p>getReportd</p>
     *
     * @return a {@link org.opennms.netmgt.reporting.service.Reportd} object.
     */
    public Reportd getReportd() {
        return m_reportd;
    }


    /**
     * <p>setReportd</p>
     *
     * @param reportd a {@link org.opennms.netmgt.reporting.service.Reportd} object.
     */
    public void setReportd(Reportd reportd) {
        m_reportd = reportd;
    }

}
