package org.opennms.netmgt.reporting.service;


import java.util.Arrays;
import java.util.Iterator;
import java.text.ParseException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.StringUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.ReportdConfigurationDao;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;


/**
 * <p>ReportScheduler class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportScheduler implements InitializingBean {

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
    public void afterPropertiesSet() throws Exception {
        
        
        try {
            getScheduler().setJobFactory(getReportJobFactory());
        } catch (SchedulerException e) {
            LogUtils.errorf(this, e, "afterPropertiesSet: couldn't set proper JobFactory for scheduler: %s", e.getMessage());
        }
    }

    ReportScheduler(Scheduler sched){
        m_scheduler = sched;
    }

    /**
     * <p>rebuildReportSchedule</p>
     */
    public void rebuildReportSchedule() {
        
        LogUtils.infof(this,"rebuildReportSchedule: obtaining lock...");


        synchronized (m_lock) {
            LogUtils.debugf(this,"rebuildReportSchedule: lock acquired. reloading configuration...");

            try {
                m_configDao.reloadConfiguration();

                LogUtils.debugf(this,"rebuildReportSchedule: removing current report jobs from schedule...");
                removeCurrentJobsFromSchedule();

                LogUtils.debugf(this,"rebuildReportSchedule: recreating report schedule based on configuration...");
                buildReportSchedule();
                
                printCurrentSchedule();

            } catch (DataAccessResourceFailureException e) {
                LogUtils.errorf(this, e, "rebuildReportSchedule: %s", e.getMessage());
                throw new IllegalStateException(e);

            } 

        }

        LogUtils.infof(this,"rebuildReportSchedule: schedule rebuilt and lock released.");
   
    }
    
    private void printCurrentSchedule() {
        try {
            
            
            LogUtils.infof(this,"calendarNames: %s", StringUtils.arrayToCommaDelimitedString(getScheduler().getCalendarNames()));
            LogUtils.infof(this,"current executing jobs: %s", StringUtils.arrayToCommaDelimitedString(getScheduler().getCurrentlyExecutingJobs().toArray()));
            LogUtils.infof(this,"current job names: %s", StringUtils.arrayToCommaDelimitedString(getScheduler().getJobNames(JOB_GROUP)));
            LogUtils.infof(this,"scheduler metadata: %s", getScheduler().getMetaData());
            LogUtils.infof(this,"trigger names: %s", StringUtils.arrayToCommaDelimitedString(getScheduler().getTriggerNames(JOB_GROUP)));

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
                sb.append(t.getJobDataMap().get(ReportJob.KEY));
                sb.append(", next fire time: ");
                sb.append(t.getNextFireTime());
                sb.append(", previous fire time: ");
                sb.append(t.getPreviousFireTime());
                sb.append(", time zone: ");
                sb.append(t.getTimeZone());
                sb.append(", priority: ");
                sb.append(t.getPriority());
                LogUtils.infof(this, sb.toString());
            }

        } catch (Exception e) {
            LogUtils.errorf(this, e, "printCurrentSchedule: %s", e.getMessage());
        }

        
    }

    private void buildReportSchedule() {

        synchronized (m_lock) {

            for(Report report : m_configDao.getReports()) {
                JobDetail detail = null;
                Trigger trigger = null;

                try {
                    detail = new JobDetail(report.getReportName(), JOB_GROUP, ReportJob.class, false, false, false);
                    detail.getJobDataMap().put(ReportJob.KEY, report);
                    trigger = new CronTrigger(report.getReportName(), JOB_GROUP, report.getCronSchedule());
                    trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
                    getScheduler().scheduleJob(detail, trigger);

                } catch (ParseException e) {
                    LogUtils.errorf(this, e, "buildReportSchedule: %s", e.getMessage());
                } catch (SchedulerException e) {
                    LogUtils.errorf(this, e, "buildReportSchedule: %s", e.getMessage());
                }
            }
        }
    }

    
    private void removeCurrentJobsFromSchedule()  {
        synchronized (m_lock) {
            try {

            Iterator<String> it = Arrays.asList(m_scheduler.getJobNames(JOB_GROUP)).iterator();
            while (it.hasNext()) {
                String jobName = it.next();
               
                    getScheduler().deleteJob(jobName, JOB_GROUP);
            }
        
            } catch (SchedulerException e) {
                    LogUtils.errorf(this, e, "removeCurrentJobsFromSchedule: %s", e.getMessage());
                }
            }        
    }

    /**
     * <p>getConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ReportdConfigurationDao} object.
     */
    public ReportdConfigurationDao getConfigDao() {
        return m_configDao;
    }


    /**
     * <p>setConfigDao</p>
     *
     * @param configDao a {@link org.opennms.netmgt.dao.ReportdConfigurationDao} object.
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
    
}
