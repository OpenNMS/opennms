/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
