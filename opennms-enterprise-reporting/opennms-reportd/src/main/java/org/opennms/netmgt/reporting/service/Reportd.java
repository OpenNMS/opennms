package org.opennms.netmgt.reporting.service;

import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
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

    public void start() throws Exception {
           m_reportScheduler.start();
    }

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
    }
   
    
    public void runReport(String reportName){
        runReport(reportName,null);
    }
 
    
    public void runReport(String reportName, String[] reportEmailDestinations) {
        LogUtils.debugf(this, "reportd -- running job %s", reportName );
    }
 
    
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(Event e) {

        if (isReloadConfigEventTarget(e)) {
            LogUtils.infof(this,"handleReloadConfigEvent: reloading configuration...");
            EventBuilder ebldr = null;

            try {
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

   
    
}
