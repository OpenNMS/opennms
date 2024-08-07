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

import java.util.List;
import java.util.Map;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.ReportdConfigurationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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
    	Map<String,String> mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putPrefix(NAME);
            LOG.debug("reportd -- running job {}", report.getReportName());
            String fileName = m_reportService.runReport(report,reportDirectory);
            if (report.getRecipients().size() > 0) {
                LOG.debug("reportd -- delivering report {} to {} recipients", report.getReportName(), report.getRecipients().size());
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
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.REPORTD_RUN_REPORT)
    public void handleRunReportEvent(IEvent e){
       String reportName = "";
       
       for(IParm parm : e.getParmCollection()){
       
           if(EventConstants.PARM_REPORT_NAME.equals(parm.getParmName()))
               reportName = parm.getValue().getContent();
           
           else 
               LOG.info("Unknown Event Constant: {}",parm.getParmName());
               
           }
           
           if (!"".equals(reportName)){
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
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(IEvent e) {

        if (isReloadConfigEventTarget(e)) {
            LOG.info("handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            try {
                
                reportDirectory = m_reportConfigurationDao.getStorageDirectory();
                
                LOG.debug("handleReloadConfigEvent: lock acquired, unscheduling current reports...");

                m_reportScheduler.rebuildReportSchedule();

                LOG.debug("handleReloadConfigEvent: reports rescheduled.");
                
                m_reportDeliveryService.reloadConfiguration();
                
                LOG.debug("handleReloadConfigEvent: Configuration reloaded for report delivery service {}", m_reportDeliveryService.getClass().getName());

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

    private boolean isReloadConfigEventTarget(IEvent event) {
        boolean isTarget = false;

        List<IParm> parmCollection = event.getParmCollection();

        for (IParm parm : parmCollection) {
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
