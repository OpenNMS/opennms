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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.ReportdConfigurationDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

/**
 * <p>Reportd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@EventListener(name="Reportd:EventListener", logPrefix="reportd")
public class Reportd implements SpringServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(Reportd.class);

    /** Constant <code>NAME="Reportd"</code> */
    public static final String NAME = "reportd";
    
    private volatile EventForwarder m_eventForwarder;
    private ReportScheduler m_reportScheduler;
    private ReportService m_reportService;
    private ReportDeliveryService m_reportDeliveryService;
    private ReportdConfigurationDao m_reportConfigurationDao;
    
    private String reportDirectory;

    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void start() throws Exception {
          reportDirectory = m_reportConfigurationDao.getStorageDirectory();

          m_reportScheduler.start();
    }

    /**
     * <p>destroy</p>
     */
    @Override
    public void destroy() throws Exception {
          m_reportScheduler.destroy();
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {    
        Assert.notNull(m_eventForwarder, "No Event Forwarder Set");
        Assert.notNull(m_reportScheduler, "No Report Scheduler Set");
        Assert.notNull(m_reportService,"No Report service set");
        Assert.notNull(m_reportDeliveryService,"No Delivery service set");
        Assert.notNull(m_reportConfigurationDao,"NoConfiguration DAO Defined");
    }
   
    
    /**
     * <p>runReport</p>
     *
     * @param reportName a {@link java.lang.String} object.
     */
    public void runReport(String reportName){
        LOG.info("Running report by name: ({}).", reportName);
        runReport(m_reportConfigurationDao.getReport(reportName));
    }
      
    /**
     * <p>runReport</p>
     *
     * @param report a {@link org.opennms.netmgt.config.reportd.Report} object.
     */
    public void runReport(Report report) {
    	Map mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putPrefix(NAME);
            LOG.debug("reportd -- running job {}", report.getReportName());
            String fileName = m_reportService.runReport(report,reportDirectory);
            if (report.getRecipientCount() > 0) {
                LOG.debug("reportd -- delivering report {} to {} recipients", report.getReportName(), report.getRecipientCount());
                m_reportDeliveryService.deliverReport(report, fileName);
            } else {
                LOG.info("Skipped delivery of report {} because it has no recipients", report.getReportName());
            }
            LOG.debug("reportd -- done running job {}",report.getReportName());
        } catch (ReportRunException e) {
            createAndSendReportingEvent(EventConstants.REPORT_RUN_FAILED_UEI, report.getReportName(), e.getMessage());
        } catch (ReportDeliveryException e) {
            createAndSendReportingEvent(EventConstants.REPORT_DELIVERY_FAILED_UEI, report.getReportName(), e.getMessage());
        } finally {        
        	Logging.setContextMap(mdc);
        }
    }
    
    /**
     * <p>createAndSendReportingEvent
     * 
     * @param uei the UEI of the event to send
     * @param reportName the name of the report in question
     * @param reason an explanation of why this event was sent
     */
    private void createAndSendReportingEvent(String uei, String reportName, String reason) {
        LOG.debug("Crafting reporting event with UEI '{}' for report '{}' with reason '{}'", uei, reportName, reason);
        
        EventBuilder bldr = new EventBuilder(uei, NAME);
        bldr.addParam(EventConstants.PARM_REPORT_NAME, reportName);
        bldr.addParam(EventConstants.PARM_REASON, reason);
        m_eventForwarder.sendNow(bldr.getEvent());
    }
 
    
    /**
     * <p>handleRunReportEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.REPORTD_RUN_REPORT)
    public void handleRunReportEvent(Event e){
       String reportName = new String();
       
       for(Parm parm : e.getParmCollection()){
       
           if(EventConstants.PARM_REPORT_NAME.equals(parm.getParmName()))
               reportName = parm.getValue().getContent();
           
           else 
               LOG.info("Unknown Event Constant: {}",parm.getParmName());
               
           }
           
           if (reportName != ""){
              LOG.debug("running report {}", reportName);
              runReport(reportName);
               
           }
           else {
               LOG.error("Can not run report -- reportName not specified");
           }
       }
 
    
    /**
     * <p>handleReloadConfigEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {

        if (isReloadConfigEventTarget(e)) {
            LOG.info("handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            try {
                
                reportDirectory = m_reportConfigurationDao.getStorageDirectory();
                
                LOG.debug("handleReloadConfigEvent: lock acquired, unscheduling current reports...");

                m_reportScheduler.rebuildReportSchedule();

                LOG.debug("handleRelodConfigEvent: reports rescheduled.");

                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Reportd");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Reportd");

            } catch (Throwable ex) {

                LOG.error("handleReloadConfigurationEvent: Error reloading configuration: {}", ex.getMessage(), ex);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Reportd");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Reportd");
                ebldr.addParam(EventConstants.PARM_REASON, ex.getLocalizedMessage().substring(1, 128));

            }

            if (ebldr != null) {
                getEventForwarder().sendNow(ebldr.getEvent());
            }
            LOG.info("handleReloadConfigEvent: configuration reloaded.");
        }

    }

    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;

        List<Parm> parmCollection = event.getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Reportd".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }

        LOG.debug("isReloadConfigEventTarget: Reportd was target of reload event: {}", isTarget);
        return isTarget;
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
    /**
     * <p>setReportScheduler</p>
     *
     * @param reportScheduler a {@link org.opennms.netmgt.reporting.service.ReportScheduler} object.
     */
    public void setReportScheduler(ReportScheduler reportScheduler) {
        m_reportScheduler = reportScheduler;
    }
    /**
     * <p>getReportScheduler</p>
     *
     * @return a {@link org.opennms.netmgt.reporting.service.ReportScheduler} object.
     */
    public ReportScheduler getReportScheduler() {
        return m_reportScheduler;
    }

    /**
     * <p>getReportService</p>
     *
     * @return a {@link org.opennms.netmgt.reporting.service.ReportService} object.
     */
    public ReportService getReportService() {
        return m_reportService;
    }

    /**
     * <p>setReportService</p>
     *
     * @param reportService a {@link org.opennms.netmgt.reporting.service.ReportService} object.
     */
    public void setReportService(ReportService reportService) {
        m_reportService = reportService;
    }

    /**
     * <p>getReportDeliveryService</p>
     *
     * @return a {@link org.opennms.netmgt.reporting.service.ReportDeliveryService} object.
     */
    public ReportDeliveryService getReportDeliveryService() {
        return m_reportDeliveryService;
    }

    /**
     * <p>setReportDeliveryService</p>
     *
     * @param reportDeliveryService a {@link org.opennms.netmgt.reporting.service.ReportDeliveryService} object.
     */
    public void setReportDeliveryService(
            ReportDeliveryService reportDeliveryService) {
        m_reportDeliveryService = reportDeliveryService;
    }

    /**
     * <p>getReportdConfigurationDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ReportdConfigurationDao} object.
     */
    public ReportdConfigurationDao getReportdConfigurationDao() {
        return m_reportConfigurationDao;
    }

    /**
     * <p>setReportdConfigurationDao</p>
     *
     * @param reportConfigurationDao a {@link org.opennms.netmgt.dao.api.ReportdConfigurationDao} object.
     */
    public void setReportdConfigurationDao(
            ReportdConfigurationDao reportConfigurationDao) {
        m_reportConfigurationDao = reportConfigurationDao;
    }
   
    
}
