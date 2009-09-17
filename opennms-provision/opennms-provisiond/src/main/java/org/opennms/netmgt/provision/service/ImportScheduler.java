/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 13, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.provision.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.ProvisiondConfigurationDao;
import org.opennms.netmgt.provision.service.dns.DnsUrlFactory;
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
 *
 */
public class ImportScheduler implements InitializingBean {
    
    protected static final String JOB_GROUP = "Provisiond";

    @Autowired
    private Scheduler m_scheduler;
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ProvisiondConfigurationDao m_configDao;

    private Object m_lock = new Object();

    private JobFactory m_importJobFactory;
    
    protected ImportScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    public void afterPropertiesSet() {
        
        try {
            getScheduler().setJobFactory(getImportJobFactory());
        } catch (SchedulerException e) {
            log().fatal("afterPropertiesSet: couldn't set proper JobFactory for scheduler: "+e, e);
        }

        
        //TODO: this needs to be done in application context
        try {
            new URL("dns://host/zone");
        } catch (MalformedURLException e) {
            URL.setURLStreamHandlerFactory(new DnsUrlFactory());
        }
        
        buildImportSchedule();
    }
    
    public void start() throws SchedulerException {
        getScheduler().start();
    }
    
    public void pause() throws SchedulerException {
        getScheduler().pauseAll();
    }
    
    public void standBy() throws SchedulerException {
        getScheduler().standby();
    }
    
    public void resume() throws SchedulerException {
        getScheduler().resumeAll();
    }
    
    public void stop() throws SchedulerException {
        getScheduler().shutdown();
    }
    
    /**
     * Removes all jobs from the current scheduled and the builds a new schedule
     * from the reloaded configuration.  Since all jobs are Cron like, removing and re-adding
     * shouldn't be an issue.
     * 
     * @throws Exception
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
     * @throws SchedulerException
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

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    public void setProvisioner(Provisioner provisioner) {
        m_provisioner = provisioner;
    }
    
    protected final Provisioner getProvisioner() {
        return m_provisioner;
    }
    
    
    public void setImportJobFactory(JobFactory importJobFactory) {
        m_importJobFactory = importJobFactory;
    }

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
            
        } catch (Exception e) {
            log().error("printCurrentSchedule: "+e.getLocalizedMessage(), e);
        }
        
    }

    private Logger log() {
        return ThreadCategory.getInstance(this.getClass());
    }


}
