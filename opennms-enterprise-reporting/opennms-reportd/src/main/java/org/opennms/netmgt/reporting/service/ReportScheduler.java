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


import java.text.ParseException;
import java.util.stream.Collectors;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.api.ReportdConfigurationDao;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.StringUtils;


/**
 * <p>ReportScheduler class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportScheduler implements InitializingBean, DisposableBean {
	
	
	private static final Logger LOG = LoggerFactory
			.getLogger(ReportScheduler.class);

    /** Constant <code>JOB_GROUP="Reportd"</code> */
    protected static final String JOB_GROUP = "Reportd";
    
    @Autowired
    private ReportdConfigurationDao m_configDao;

    @Autowired
    private Scheduler m_scheduler;

    private JobFactory m_reportJobFactory;

    private Object m_lock = new Object();
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);

        try {
            getScheduler().setJobFactory(getReportJobFactory());
        } catch (SchedulerException e) {
            LOG.error("afterPropertiesSet: couldn't set proper JobFactory for scheduler: {}", e.getMessage(), e);
        }
    }

    ReportScheduler(Scheduler sched){
        m_scheduler = sched;
    }

    /**
     * <p>rebuildReportSchedule</p>
     */
    public void rebuildReportSchedule() {
        
        LOG.info("rebuildReportSchedule: obtaining lock...");


        synchronized (m_lock) {
            LOG.debug("rebuildReportSchedule: lock acquired. reloading configuration...");

            try {
                m_configDao.reloadConfiguration();

                LOG.debug("rebuildReportSchedule: removing current report jobs from schedule...");
                removeCurrentJobsFromSchedule();

                LOG.debug("rebuildReportSchedule: recreating report schedule based on configuration...");
                buildReportSchedule();
                
                printCurrentSchedule();

            } catch (DataAccessResourceFailureException e) {
                LOG.error("rebuildReportSchedule: {}", e.getMessage(), e);
                throw new IllegalStateException(e);

            } 

        }

        LOG.info("rebuildReportSchedule: schedule rebuilt and lock released.");
   
    }
    
    private void printCurrentSchedule() {
        try {
            LOG.info("calendarNames: {}", String.join(", ", getScheduler().getCalendarNames().toArray(new String[0])));
            LOG.info("current executing jobs: {}", StringUtils.arrayToCommaDelimitedString(getScheduler().getCurrentlyExecutingJobs().toArray()));
            LOG.info("current job names: {}", getScheduler().getJobKeys(GroupMatcher.<JobKey>groupEquals(JOB_GROUP)).stream().map(JobKey::getName).collect(Collectors.joining(", ")));
            LOG.info("scheduler metadata: {}", getScheduler().getMetaData());
            LOG.info("trigger names: {}", getScheduler().getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(JOB_GROUP)).stream().map(TriggerKey::getName).collect(Collectors.joining(", ")));
            
            for (TriggerKey key : getScheduler().getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(JOB_GROUP))) {
                CronTrigger t = (CronTrigger)getScheduler().getTrigger(key);
                StringBuilder sb = new StringBuilder("trigger: ");
                sb.append(key.getName());
                sb.append(", calendar name: ");
                sb.append(t.getCalendarName());
                sb.append(", cron expression: ");
                sb.append(t.getCronExpression());
                sb.append(", URL: ");
                sb.append(t.getJobDataMap().get(ReportJob.KEY));
                sb.append(", next fire time: ");
                sb.append(t.getNextFireTime());
                sb.append(", previous fire time: ");
                sb.append(t.getPreviousFireTime());
                sb.append(", time zone: ");
                sb.append(t.getTimeZone());
                sb.append(", priority: ");
                sb.append(t.getPriority());
                LOG.info(sb.toString());
            }

        } catch (Throwable e) {
            LOG.error("printCurrentSchedule: {}", e.getMessage(), e);
        }

        
    }

    private void buildReportSchedule() {

        synchronized (m_lock) {

            for(Report report : m_configDao.getReports()) {
                JobDetail detail = null;
                CronTriggerImpl trigger = null;

                try {
                    detail = new JobDetailImpl(report.getReportName(), JOB_GROUP, ReportJob.class, false, false);
                    detail.getJobDataMap().put(ReportJob.KEY, report);
                    trigger = new CronTriggerImpl(report.getReportName(), JOB_GROUP, report.getCronSchedule());
                    trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
                    getScheduler().scheduleJob(detail, trigger);

                } catch (ParseException e) {
                    LOG.error("buildReportSchedule: {}", e.getMessage(), e);
                } catch (SchedulerException e) {
                    LOG.error("buildReportSchedule: {}", e.getMessage(), e);
                }
            }
        }
    }

    
    private void removeCurrentJobsFromSchedule()  {
        synchronized (m_lock) {
            try {
                for (JobKey key : m_scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(JOB_GROUP))) {
                    getScheduler().deleteJob(key);
                }
            } catch (SchedulerException e) {
                LOG.error("removeCurrentJobsFromSchedule: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * <p>getConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ReportdConfigurationDao} object.
     */
    public ReportdConfigurationDao getConfigDao() {
        return m_configDao;
    }


    /**
     * <p>setConfigDao</p>
     *
     * @param configDao a {@link org.opennms.netmgt.dao.api.ReportdConfigurationDao} object.
     */
    public void setConfigDao(ReportdConfigurationDao configDao) {
        m_configDao = configDao;
    }

    
    /**
     * <p>getScheduler</p>
     *
     * @return a {@link org.quartz.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    
    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.quartz.Scheduler} object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }


    /**
     * <p>setReportJobFactory</p>
     *
     * @param reportJobFactory a {@link org.quartz.spi.JobFactory} object.
     */
    public void setReportJobFactory(JobFactory reportJobFactory) {
        m_reportJobFactory = reportJobFactory;
    }


    /**
     * <p>getReportJobFactory</p>
     *
     * @return a {@link org.quartz.spi.JobFactory} object.
     */
    public JobFactory getReportJobFactory() {
        return m_reportJobFactory;
    }
    

    /**
     * <p>start</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    public void start() throws SchedulerException {
        getScheduler().start();
        buildReportSchedule();
        printCurrentSchedule();
    }

    /**
     * <p>destroy</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    @Override
    public void destroy() throws SchedulerException {
        getScheduler().shutdown();
    }
}
