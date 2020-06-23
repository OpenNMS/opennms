/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import java.text.ParseException;

import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * <p>Statsd class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
@EventListener(name="OpenNMS:Statsd", logPrefix="statsd")
public class Statsd implements SpringServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Statsd.class);

    private static final String LOG4J_CATEGORY = "statsd";

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    private MeasurementFetchStrategy m_fetchStrategy;

    @Autowired
    private FilterDao m_filterDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    private ReportPersister m_reportPersister;
    private Scheduler m_scheduler;
    private ReportDefinitionBuilder m_reportDefinitionBuilder;
    private volatile EventForwarder m_eventForwarder;
    
    private long m_reportsStarted = 0;
    private long m_reportsCompleted = 0;
    private long m_reportsPersisted = 0;
    private long m_reportRunTime = 0;

    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {
        
        if (isReloadConfigEventTarget(e)) {
            LOG.info("handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            LOG.debug("handleReloadConfigEvent: acquiring lock...");
            synchronized (m_scheduler) {
                try {
                    LOG.debug("handleReloadConfigEvent: lock acquired, unscheduling current reports...");
                    unscheduleReports();
                    m_reportDefinitionBuilder.reload();
                    LOG.debug("handleReloadConfigEvent: config remarshaled, unscheduling current reports...");
                    LOG.debug("handleReloadConfigEvent: reports unscheduled, rescheduling...");
                    start();
                    LOG.debug("handleRelodConfigEvent: reports rescheduled.");
                    ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Statsd");
                    ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Statsd");
                } catch (Throwable exception) {
                    LOG.error("handleReloadConfigurationEvent: Error reloading configuration", exception);
                    ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Statsd");
                    ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Statsd");
                    ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
                }
                if (ebldr != null) {
                    getEventForwarder().sendNow(ebldr.getEvent());
                }
            }
            LOG.debug("handleReloadConfigEvent: lock released.");
        }
        
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        if ("Statsd".equalsIgnoreCase(EventUtils.getParm(event, EventConstants.PARM_DAEMON_NAME))) {
            isTarget = true;
        }
        
        LOG.debug("isReloadConfigEventTarget: Statsd was target of reload event: {}", isTarget);
        return isTarget;
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.daemon.SpringServiceDaemon#start()
     *
     * Changed this to just throw Exception since nothing is actually done with each individual exception types.
     */
    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void start() throws Exception {
        LOG.debug("start: acquiring lock...");
        synchronized (m_scheduler) {
            LOG.info("start: lock acquired (may have reentered), scheduling Reports...");
            for (ReportDefinition reportDef : m_reportDefinitionBuilder.buildReportDefinitions()) {
                LOG.debug("start: scheduling Report: {}", reportDef);
                scheduleReport(reportDef);
            }
            LOG.info("start: {} jobs scheduled.", m_scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(Scheduler.DEFAULT_GROUP)).size());
        }
        LOG.debug("start: lock released (unless reentrant).");
    }

    @Override
    public void destroy() throws Exception {
        LOG.debug("start: acquiring lock...");
        synchronized (m_scheduler) {
            m_scheduler.shutdown();
        }
        LOG.debug("start: lock released (unless reentrant).");
    }

    /**
     * <p>unscheduleReports</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void unscheduleReports() throws Exception {
        
        synchronized (m_scheduler) {
            for (ReportDefinition reportDef : m_reportDefinitionBuilder.buildReportDefinitions()) {
                m_scheduler.deleteJob(new JobKey(reportDef.getDescription(), Scheduler.DEFAULT_GROUP));
            }
        }
    }
    
    private void scheduleReport(ReportDefinition reportDef) throws ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException, Exception {
        
        //this is most likely reentrant since the method is private and called from start via plural version.
        synchronized (m_scheduler) {
            
            MethodInvokingJobDetailFactoryBean jobFactory = new MethodInvokingJobDetailFactoryBean();
            jobFactory.setTargetObject(this);
            jobFactory.setTargetMethod("runReport");
            jobFactory.setArguments(new Object[] { reportDef });
            jobFactory.setConcurrent(false);
            jobFactory.setBeanName(reportDef.getDescription());
            jobFactory.afterPropertiesSet();
            JobDetail jobDetail = (JobDetail) jobFactory.getObject();
            
            CronTriggerFactoryBean cronReportTrigger = new CronTriggerFactoryBean();
            cronReportTrigger.setBeanName(reportDef.getDescription());
            cronReportTrigger.setJobDetail(jobDetail);
            cronReportTrigger.setCronExpression(reportDef.getCronExpression());
            cronReportTrigger.afterPropertiesSet();
            
            m_scheduler.scheduleJob(jobDetail, cronReportTrigger.getObject());
            LOG.debug("Schedule report {}", cronReportTrigger);
            
        }
    }

    /**
     * <p>runReport</p>
     *
     * @param reportDef a {@link org.opennms.netmgt.statsd.ReportDefinition} object.
     * @throws java.lang.Throwable if any.
     */
    public void runReport(ReportDefinition reportDef) throws Throwable {
        final ReportInstance report;
        try {
            report = reportDef.createReport(m_nodeDao, m_resourceDao, m_fetchStrategy, m_filterDao);
        } catch (Throwable t) {
            LOG.error("Could not create a report instance for report definition {}", reportDef, t);
            throw t;
        }
        
        // FIXME What if the walker or the persister throws an exception ?
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                long reportStartTime = System.currentTimeMillis();
                LOG.debug("Starting report {}", report);
                accountReportStart();
                report.walk();
                LOG.debug("Completed report {}", report);
                accountReportComplete();
                
                m_reportPersister.persist(report);
                LOG.debug("Report {} persisted", report);
                accountReportPersist();
                accountReportRunTime(System.currentTimeMillis() - reportStartTime);
            }
        });
    }

    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_nodeDao != null, "property nodeDao must be set to a non-null value");
        Assert.state(m_resourceDao != null, "property resourceDao must be set to a non-null value");
        Assert.state(m_fetchStrategy != null, "property fetchStrategy must be set to a non-null value");
        Assert.state(m_filterDao != null, "property filterDao must be set to a non-null value");
        Assert.state(m_transactionTemplate != null, "property transactionTemplate must be set to a non-null value");
        Assert.state(m_reportPersister != null, "property reportPersister must be set to a non-null value");
        Assert.state(m_scheduler != null, "property scheduler must be set to a non-null value");
        Assert.state(m_reportDefinitionBuilder != null, "property reportDefinitionBuilder must be set to a non-null value");
        Assert.state(m_eventForwarder != null, "eventForwarder property must be set to a non-null value");
    }

    /**
     * @return the nodeDao
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public MeasurementFetchStrategy getFetchStrategy() {
        return m_fetchStrategy;
    }

    public void setFetchStrategy(final MeasurementFetchStrategy fetchStrategy) {
        this.m_fetchStrategy = fetchStrategy;
    }

    /**
     * <p>getTransactionTemplate</p>
     *
     * @return a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public TransactionTemplate getTransactionTemplate() {
        return m_transactionTemplate;
    }

    /**
     * <p>getReportPersister</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportPersister} object.
     */
    public ReportPersister getReportPersister() {
        return m_reportPersister;
    }

    /**
     * <p>setReportPersister</p>
     *
     * @param reportPersister a {@link org.opennms.netmgt.statsd.ReportPersister} object.
     */
    public void setReportPersister(ReportPersister reportPersister) {
        m_reportPersister = reportPersister;
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
     * <p>getReportDefinitionBuilder</p>
     *
     * @return a {@link org.opennms.netmgt.statsd.ReportDefinitionBuilder} object.
     */
    public ReportDefinitionBuilder getReportDefinitionBuilder() {
        return m_reportDefinitionBuilder;
    }

    /**
     * <p>setReportDefinitionBuilder</p>
     *
     * @param reportDefinitionBuilder a {@link org.opennms.netmgt.statsd.ReportDefinitionBuilder} object.
     */
    public void setReportDefinitionBuilder(ReportDefinitionBuilder reportDefinitionBuilder) {
        m_reportDefinitionBuilder = reportDefinitionBuilder;
    }

    /**
     * <p>getFilterDao</p>
     *
     * @return a {@link org.opennms.netmgt.filter.api.FilterDao} object.
     */
    public FilterDao getFilterDao() {
        return m_filterDao;
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    } 

    private synchronized void accountReportStart() {
        m_reportsStarted++;
    }

    private synchronized void accountReportComplete() {
        m_reportsCompleted++;
    }

    private synchronized void accountReportPersist() {
        m_reportsPersisted++;
    }

    private synchronized void accountReportRunTime(long runtime) {
        m_reportRunTime += runtime;
    }

    public long getReportsStarted() {
        return m_reportsStarted;
    }

    public long getReportsCompleted() {
        return m_reportsCompleted;
    }

    public long getReportsPersisted() {
        return m_reportsPersisted;
    }

    public long getReportRunTime() {
        return m_reportRunTime;
    }
}
