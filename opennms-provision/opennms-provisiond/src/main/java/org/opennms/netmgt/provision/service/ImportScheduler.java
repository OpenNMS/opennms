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

package org.opennms.netmgt.provision.service;

import java.text.ParseException;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.StringUtils;

/**
 * Maintains the Provisioner's import schedule defined in provisiond-configuration.xml
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class ImportScheduler implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ImportScheduler.class);
    
    /** Constant <code>JOB_GROUP="Provisiond"</code> */
    protected static final String JOB_GROUP = "Provisiond";

    @Autowired
    private Scheduler m_scheduler;
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ProvisiondConfigurationDao m_configDao;

    private Object m_lock = new Object();

    private JobFactory m_importJobFactory;
    
    /**
     * <p>Constructor for ImportScheduler.</p>
     *
     * @param scheduler a {@link org.quartz.Scheduler} object.
     */
    protected ImportScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        
        try {
            getScheduler().setJobFactory(getImportJobFactory());
        } catch (SchedulerException e) {
            LOG.error("afterPropertiesSet: couldn't set proper JobFactory for scheduler", e);
        }

        GenericURLFactory.initialize();

        buildImportSchedule();
    }
    
    /**
     * <p>start</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    public void start() throws SchedulerException {
        getScheduler().start();
    }
    
    /**
     * <p>pause</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    public void pause() throws SchedulerException {
        getScheduler().pauseAll();
    }
    
    /**
     * <p>standBy</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    public void standBy() throws SchedulerException {
        getScheduler().standby();
    }
    
    /**
     * <p>resume</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    public void resume() throws SchedulerException {
        getScheduler().resumeAll();
    }
    
    /**
     * <p>stop</p>
     *
     * @throws org.quartz.SchedulerException if any.
     */
    public void stop() throws SchedulerException {
        getScheduler().shutdown();
    }
    
    /**
     * Removes all jobs from the current scheduled and the builds a new schedule
     * from the reloaded configuration.  Since all jobs are Cron like, removing and re-adding
     * shouldn't be an issue.
     *
     * @throws java.lang.Exception if any.
     */
    protected void rebuildImportSchedule() throws Exception {
        
        LOG.info("rebuildImportSchedule: acquiring lock...");

        synchronized (m_lock) {
            LOG.debug("rebuildImportSchedule: lock acquired.  reloading configuration.");
            
            try {
                m_configDao.reloadConfiguration();
                
                LOG.debug("rebuildImportSchedule: removing current import jobs from schedule...");
                removeCurrentJobsFromSchedule();
                
                LOG.debug("rebuildImportSchedule: recreating import schedule based on configuration...");
                buildImportSchedule();
                
                printCurrentSchedule();
                
            } catch (DataAccessResourceFailureException e) {
                LOG.error("rebuildImportSchedule: {}", e.getLocalizedMessage(),e);
                throw new IllegalStateException(e);
                
            } catch (SchedulerException e) {
                LOG.error("rebuildImportSchedule: {}", e.getLocalizedMessage(),e);
                throw e;
            }
            
        }
        
        LOG.info("rebuildImportSchedule: schedule rebuilt and lock released.");
    }

    /**
     * Iterates of current job list and removes each job from the underlying schedule
     *
     * @throws org.quartz.SchedulerException if any.
     */
    protected void removeCurrentJobsFromSchedule() throws SchedulerException {
        
        printCurrentSchedule();
        synchronized (m_lock) {
            
            for (JobKey key : m_scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(JOB_GROUP))) {
                String jobName = key.getName();
                try {
                    
                    getScheduler().deleteJob(new JobKey(jobName, JOB_GROUP));
                } catch (SchedulerException e) {
                    LOG.error("removeCurrentJobsFromSchedule: {}", e.getLocalizedMessage(), e);
                }
            }
        }
        printCurrentSchedule();
    }

    /**
     * <p>buildImportSchedule</p>
     */
    protected void buildImportSchedule() {
        
        synchronized (m_lock) {

            Iterator<RequisitionDef> it = m_configDao.getDefs().iterator();
            
            while (it.hasNext()) {
                RequisitionDef def = it.next();
                JobDetail detail = null;
                CronTriggerImpl trigger = null;
                
                try {
                    detail = new JobDetailImpl(def.getImportName(), JOB_GROUP, ImportJob.class, false, false);
                    detail.getJobDataMap().put(ImportJob.URL, def.getImportUrlResource());
                    detail.getJobDataMap().put(ImportJob.RESCAN_EXISTING, def.getRescanExisting());
                    
                    trigger = new CronTriggerImpl(def.getImportName(), JOB_GROUP, def.getCronSchedule());
                    trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
                    getScheduler().scheduleJob(detail, trigger);
                    
                } catch (ParseException e) {
                    LOG.error("buildImportSchedule: {}", e.getLocalizedMessage(), e);
                } catch (SchedulerException e) {
                    LOG.error("buildImportSchedule: {}", e.getLocalizedMessage(), e);
                }                
            }
        }
        
        printCurrentSchedule();

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
     * <p>setProvisioner</p>
     *
     * @param provisioner a {@link org.opennms.netmgt.provision.service.Provisioner} object.
     */
    public void setProvisioner(Provisioner provisioner) {
        m_provisioner = provisioner;
    }
    
    /**
     * <p>getProvisioner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.Provisioner} object.
     */
    protected final Provisioner getProvisioner() {
        return m_provisioner;
    }
    
    
    /**
     * <p>setImportJobFactory</p>
     *
     * @param importJobFactory a {@link org.quartz.spi.JobFactory} object.
     */
    public void setImportJobFactory(JobFactory importJobFactory) {
        m_importJobFactory = importJobFactory;
    }

    /**
     * <p>getImportJobFactory</p>
     *
     * @return a {@link org.quartz.spi.JobFactory} object.
     */
    public JobFactory getImportJobFactory() {
        return m_importJobFactory;
    }

    private void printCurrentSchedule() {
        
        try {
            LOG.info("calendarNames: {}", String.join(", ", getScheduler().getCalendarNames().toArray(new String[0])));
            LOG.info("current executing jobs: {}", StringUtils.arrayToCommaDelimitedString(getScheduler().getCurrentlyExecutingJobs().toArray()));
            LOG.info("current job names: {}", getScheduler().getJobKeys(GroupMatcher.<JobKey>groupEquals(JOB_GROUP)).stream().map(JobKey::getName).collect(Collectors.joining(", ")));
            LOG.info("scheduler metadata: {}", getScheduler().getMetaData());
            LOG.info("trigger names: {}", getScheduler().getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(JOB_GROUP)).stream().map(TriggerKey::getName).collect(Collectors.joining(", ")));
            
            for (TriggerKey key : getScheduler().getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(JOB_GROUP))) {
                String triggerName = key.getName();
                CronTrigger t = (CronTrigger) getScheduler().getTrigger(key);
                LOG.info("trigger: {}, calendar name: {}, cron expression: {}, URL: {}, rescanExisting: {}, next fire time: {}, previous fire time: {}, time zone: {}, priority: {}",
                         triggerName,
                         t.getCalendarName(),
                         t.getCronExpression(),
                         t.getJobDataMap().get(ImportJob.URL),
                         t.getJobDataMap().get(ImportJob.RESCAN_EXISTING),
                         t.getNextFireTime(),
                         t.getPreviousFireTime(),
                         t.getTimeZone(),
                         t.getPriority());
            }
            
        } catch (Throwable e) {
            LOG.error("printCurrentSchedule: {}", e.getLocalizedMessage(), e);
        }
        
    }
}
