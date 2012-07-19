/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import java.text.ParseException;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.CronTriggerBean;
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
@EventListener(name="OpenNMS:Statsd")
public class Statsd implements SpringServiceDaemon {
    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private RrdDao m_rrdDao;
    private FilterDao m_filterDao;
    private TransactionTemplate m_transactionTemplate;
    private ReportPersister m_reportPersister;
    private Scheduler m_scheduler;
    private ReportDefinitionBuilder m_reportDefinitionBuilder;
    private volatile EventForwarder m_eventForwarder;

    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei=EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {
        
        if (isReloadConfigEventTarget(e)) {
            log().info("handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            log().debug("handleReloadConfigEvent: acquiring lock...");
            synchronized (m_scheduler) {
                try {
                    log().debug("handleReloadConfigEvent: lock acquired, unscheduling current reports...");
                    unscheduleReports();
                    m_reportDefinitionBuilder.reload();
                    log().debug("handleReloadConfigEvent: config remarshaled, unscheduling current reports...");
                    log().debug("handleReloadConfigEvent: reports unscheduled, rescheduling...");
                    start();
                    log().debug("handleRelodConfigEvent: reports rescheduled.");
                    ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Statsd");
                    ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Statsd");
                } catch (Throwable exception) {
                    log().error("handleReloadConfigurationEvent: Error reloading configuration:"+exception, exception);
                    ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Statsd");
                    ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Statsd");
                    ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
                }
                if (ebldr != null) {
                    getEventForwarder().sendNow(ebldr.getEvent());
                }
            }
            log().debug("handleReloadConfigEvent: lock released.");
        }
        
    }
    
    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;
        
        List<Parm> parmCollection = event.getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Statsd".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        
        log().debug("isReloadConfigEventTarget: Statsd was target of reload event: "+isTarget);
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
        log().debug("start: acquiring lock...");
        synchronized (m_scheduler) {
            log().info("start: lock acquired (may have reentered), scheduling Reports...");
            for (ReportDefinition reportDef : m_reportDefinitionBuilder.buildReportDefinitions()) {
                log().debug("start: scheduling Report: "+reportDef+"...");
                scheduleReport(reportDef);
            }
            log().info("start: "+m_scheduler.getJobNames(Scheduler.DEFAULT_GROUP).length+" jobs scheduled.");
        }
        log().debug("start: lock released (unless reentrant).");
    }

    @Override
    public void destroy() throws Exception {
        log().debug("start: acquiring lock...");
        synchronized (m_scheduler) {
            m_scheduler.shutdown();
        }
        log().debug("start: lock released (unless reentrant).");
    }

    /**
     * <p>unscheduleReports</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void unscheduleReports() throws Exception {
        
        synchronized (m_scheduler) {
            for (ReportDefinition reportDef : m_reportDefinitionBuilder.buildReportDefinitions()) {
                m_scheduler.deleteJob(reportDef.getDescription(), Scheduler.DEFAULT_GROUP);
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
            
            CronTriggerBean cronReportTrigger = new CronTriggerBean();
            cronReportTrigger.setBeanName(reportDef.getDescription());
            cronReportTrigger.setJobDetail(jobDetail);
            cronReportTrigger.setCronExpression(reportDef.getCronExpression());
            cronReportTrigger.afterPropertiesSet();
            
            m_scheduler.scheduleJob(cronReportTrigger.getJobDetail(), cronReportTrigger);
            log().debug("Schedule report " + cronReportTrigger);
            
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
            report = reportDef.createReport(m_nodeDao, m_resourceDao, m_rrdDao, m_filterDao);
        } catch (Throwable t) {
            log().error("Could not create a report instance for report definition " + reportDef + ": " + t, t);
            throw t;
        }
        
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                log().debug("Starting report " + report);
                report.walk();
                log().debug("Completed report " + report);
                
                m_reportPersister.persist(report);
                log().debug("Report " + report + " persisted");
            }
        });
    }

    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
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
        Assert.state(m_rrdDao != null, "property rrdDao must be set to a non-null value");
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
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>getRrdDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.RrdDao} object.
     */
    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    /**
     * <p>setRrdDao</p>
     *
     * @param rrdDao a {@link org.opennms.netmgt.dao.RrdDao} object.
     */
    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
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
     * <p>setTransactionTemplate</p>
     *
     * @param transactionTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
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
     * @return a {@link org.opennms.netmgt.filter.FilterDao} object.
     */
    public FilterDao getFilterDao() {
        return m_filterDao;
    }

    /**
     * <p>setFilterDao</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.filter.FilterDao} object.
     */
    public void setFilterDao(FilterDao filterDao) {
        m_filterDao = filterDao;
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
}
