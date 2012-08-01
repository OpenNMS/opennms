/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.ProvisiondConfigurationDao;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
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
            log().fatal("afterPropertiesSet: couldn't set proper JobFactory for scheduler: "+e, e);
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
        
        log().info("rebuildImportSchedule: acquiring lock...");

        synchronized (m_lock) {
            log().debug("rebuildImportSchedule: lock acquired.  reloading configuration.");
            
            try {
                m_configDao.reloadConfiguration();
                
                log().debug("rebuildImportSchedule: removing current import jobs from schedule...");
                removeCurrentJobsFromSchedule();
                
                log().debug("rebuildImportSchedule: recreating import schedule based on configuration...");
                buildImportSchedule();
                
                printCurrentSchedule();
                
            } catch (DataAccessResourceFailureException e) {
                log().error("rebuildImportSchedule: "+e.getLocalizedMessage(),e);
                throw new IllegalStateException(e);
                
            } catch (SchedulerException e) {
                log().error("rebuildImportSchedule: "+e.getLocalizedMessage(),e);
                throw e;
            }
            
        }
        
        log().info("rebuildImportSchedule: schedule rebuilt and lock released.");
    }

    /**
     * Iterates of current job list and removes each job from the underlying schedule
     *
     * @throws org.quartz.SchedulerException if any.
     */
    protected void removeCurrentJobsFromSchedule() throws SchedulerException {
        
        printCurrentSchedule();
        synchronized (m_lock) {
            
            Iterator<String> it = Arrays.asList(m_scheduler.getJobNames(JOB_GROUP)).iterator();
            while (it.hasNext()) {
                String jobName = it.next();
                try {
                    
                    getScheduler().deleteJob(jobName, JOB_GROUP);
                } catch (SchedulerException e) {
                    log().error("removeCurrentJobsFromSchedule: "+e.getLocalizedMessage(), e);
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
                Trigger trigger = null;
                
                try {
                    detail = new JobDetail(def.getImportName(), JOB_GROUP, ImportJob.class, false, false, false);
                    detail.getJobDataMap().put(ImportJob.KEY, def.getImportUrlResource());
                    
                    trigger = new CronTrigger(def.getImportName(), JOB_GROUP, def.getCronSchedule());
                    trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
                    getScheduler().scheduleJob(detail, trigger);
                    
                } catch (ParseException e) {
                    log().error("buildImportSchedule: "+e.getLocalizedMessage(), e);
                } catch (SchedulerException e) {
                    log().error("buildImportSchedule: "+e.getLocalizedMessage(), e);
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
            log().info("calendarNames: "+ StringUtils.arrayToCommaDelimitedString(getScheduler().getCalendarNames()));
            log().info("current executing jobs: "+StringUtils.arrayToCommaDelimitedString(getScheduler().getCurrentlyExecutingJobs().toArray()));
            log().info("current job names: "+StringUtils.arrayToCommaDelimitedString(getScheduler().getJobNames(JOB_GROUP)));
            log().info("scheduler metadata: "+getScheduler().getMetaData());
            log().info("trigger names: "+StringUtils.arrayToCommaDelimitedString(getScheduler().getTriggerNames(JOB_GROUP)));
            
            Iterator<String> it = Arrays.asList(getScheduler().getTriggerNames(JOB_GROUP)).iterator();
            while (it.hasNext()) {
                String triggerName = it.next();
                CronTrigger t = (CronTrigger) getScheduler().getTrigger(triggerName, JOB_GROUP);
                StringBuilder sb = new StringBuilder("trigger: ");
                sb.append(triggerName);
                sb.append(", calendar name: ");
                sb.append(t.getCalendarName());
                sb.append(", cron expression: ");
                sb.append(t.getCronExpression());
                sb.append(", URL: ");
                sb.append(t.getJobDataMap().get(ImportJob.KEY));
                sb.append(", next fire time: ");
                sb.append(t.getNextFireTime());
                sb.append(", previous fire time: ");
                sb.append(t.getPreviousFireTime());
                sb.append(", time zone: ");
                sb.append(t.getTimeZone());
                sb.append(", priority: ");
                sb.append(t.getPriority());
                log().info(sb.toString());
            }
            
        } catch (Throwable e) {
            log().error("printCurrentSchedule: "+e.getLocalizedMessage(), e);
        }
        
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }


}
