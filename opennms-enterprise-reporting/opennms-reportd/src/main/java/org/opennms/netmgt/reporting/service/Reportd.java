package org.opennms.netmgt.reporting.service;

import java.util.List;

import org.eclipse.jdt.internal.core.Assert;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.ReportdConfigurationDao;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

@EventListener(name="Reportd:EventListener")
public class Reportd implements SpringServiceDaemon {
    
    public static final String NAME = "Reportd";
    
    private volatile EventForwarder m_eventForwarder;
    private ReportScheduler m_reportScheduler;
    private ReportService m_reportService;
    private ReportDeliveryService m_reportDeliveryService;
    private ReportdConfigurationDao m_reportConfigurationDao;
    
    private String reportDirectory;
    private boolean reportPersist;
        
    public void start() throws Exception {
          reportDirectory = m_reportConfigurationDao.getStorageDirectory();
          reportPersist = m_reportConfigurationDao.getPersistFlag();
          m_reportScheduler.start();
    }
    
    public void afterPropertiesSet() throws Exception {    
        Assert.isNotNull(m_eventForwarder, "No Event Forwarder Set");
        Assert.isNotNull(m_reportScheduler, "No Report Scheduler Set");
        Assert.isNotNull(m_reportService,"No Report service set");
        Assert.isNotNull(m_reportDeliveryService,"No Delivery service set");
        Assert.isNotNull(m_reportConfigurationDao,"NoConfiguration DAO Defined");
    }
   
    
    public void runReport(String reportName){
        LogUtils.infof(this, "Running report by name: (%s).", reportName);
        runReport(m_reportConfigurationDao.getReport(reportName));
    }
      
    public void runReport(Report report) {
            
        LogUtils.debugf(this, "reportd -- running job %s", report.getReportName() );
        String fileName = m_reportService.runReport(report,reportDirectory);
        LogUtils.debugf(this,"reportd -- delivering report %s", report.getReportName());
        m_reportDeliveryService.deliverReport(report, fileName);
        LogUtils.debugf(this,"reportd -- done running job %s",report.getReportName() );
       
    }
 
    
    @EventHandler(uei = EventConstants.REPORTD_RUN_REPORT)
    public void handleRunReportEvent(Event e){
       List <Parm> parmCollection = e.getParms().getParmCollection();
       String reportName = new String();
       
       for(Parm parm : parmCollection){
       
           if(EventConstants.PARM_REPORT_NAME.equals(parm.getParmName()))
               reportName = parm.getValue().toString();
           
           else 
               LogUtils.infof(this,"Unknown Event Constant: %s",parm.getParmName());
               
           }
           
           if (reportName != ""){
              LogUtils.debugf(this, "running report %s", reportName);
              runReport(reportName);
               
           }
           else {
               LogUtils.errorf(this, "Can not run report -- reportName not specified");
           }
       }
    
    
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {

        if (isReloadConfigEventTarget(e)) {
            LogUtils.infof(this,"handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            try {
                
                reportDirectory = m_reportConfigurationDao.getStorageDirectory();
                reportPersist = m_reportConfigurationDao.getPersistFlag();
                
                LogUtils.debugf(this,"handleReloadConfigEvent: lock acquired, unscheduling current reports...");

                m_reportScheduler.rebuildReportSchedule();

                LogUtils.debugf(this,"handleRelodConfigEvent: reports rescheduled.");

                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Reportd");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Reportd");

            } catch (Exception exception) {

                LogUtils.errorf(this,"handleReloadConfigurationEvent: Error reloading configuration:"+exception, exception);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Reportd");
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Reportd");
                ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));

            }

            if (ebldr != null) {
                getEventForwarder().sendNow(ebldr.getEvent());
            }
            LogUtils.infof(this,"handleReloadConfigEvent: configuration reloaded.");
        }

    }

    private boolean isReloadConfigEventTarget(Event event) {
        boolean isTarget = false;

        List<Parm> parmCollection = event.getParms().getParmCollection();

        for (Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Reportd".equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }

        LogUtils.debugf(this,"isReloadConfigEventTarget: Reportd was target of reload event: "+isTarget);
        return isTarget;
    }
  
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
    public void setReportScheduler(ReportScheduler reportScheduler) {
        m_reportScheduler = reportScheduler;
    }
    public ReportScheduler getReportScheduler() {
        return m_reportScheduler;
    }

    public ReportService getReportService() {
        return m_reportService;
    }

    public void setReportService(ReportService reportService) {
        m_reportService = reportService;
    }

    public ReportDeliveryService getReportDeliveryService() {
        return m_reportDeliveryService;
    }

    public void setReportDeliveryService(
            ReportDeliveryService reportDeliveryService) {
        m_reportDeliveryService = reportDeliveryService;
    }

    public ReportdConfigurationDao getReportdConfigurationDao() {
        return m_reportConfigurationDao;
    }

    public void setReportdConfigurationDao(
            ReportdConfigurationDao reportConfigurationDao) {
        m_reportConfigurationDao = reportConfigurationDao;
    }
   
    
}
