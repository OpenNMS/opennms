/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.reporting.service;


import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * <p>ReportJobFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportJobFactory implements JobFactory {

    private Reportd m_reportd;

    /** {@inheritDoc} */
    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<ReportJob> jobClass = getJobClass(jobDetail);
        
        ReportJob job = null;
        
        try {
            job = jobClass.newInstance();
            job.setReportd(getReportd());
            return job;
        } catch (Throwable e) {
            SchedulerException se = new SchedulerException("failed to create job class: "+ jobDetail.getJobClass().getName()+"; "+
                                                           e.getLocalizedMessage(), e);
            throw se;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<ReportJob> getJobClass(JobDetail jobDetail) {
        return (Class<ReportJob>)jobDetail.getJobClass();
    }

    /**
     * <p>setReportd</p>
     *
     * @param reportd a {@link org.opennms.netmgt.reporting.service.Reportd} object.
     */
    public void setReportd(Reportd reportd) {
        m_reportd = reportd;
    }
    
    private Reportd getReportd() {
        return m_reportd;
    }

}
