package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class AlarmDashlet extends Dashlet {
    
    /*
    - Transient
    - Don't need to be able to acknowledge them
    - Items to show:
        - Show if they have been acknowledged
        - Description
        - Severity (color-coded)
        - Count
        - May need to show the node label when filtering on the node label
        - Show node/interface/service (maybe as a label)
    - Sort by severity (highest first)
    - Be able to page through results (first page, previous page, next page, last page)
    - Show "Outages x - y of z"
    - Ideally be able to sort by any column

     */
    
    private AlarmView m_view = new AlarmView();
    private AlarmLoader m_loader = new AlarmLoader();
    
    class AlarmLoader extends DashletLoader implements AsyncCallback {
        
        private SurveillanceServiceAsync m_suveillanceService;
        
        public void load(final SurveillanceSet surveillanceSet) {
            loading();
            m_suveillanceService.getAlarmsForSet(surveillanceSet, this);
        }
        
        public void onDataLoaded(Alarm[] alarms) {
            try {
                m_view.setAlarms(alarms);
            } finally {
                complete();
            }
        }

        public void setSurveillanceService(SurveillanceServiceAsync svc) {
            m_suveillanceService = svc;
        }

        public void onFailure(Throwable caught) {
            loadError(caught);
            error(caught);
        }

        public void onSuccess(Object result) {
            onDataLoaded((Alarm[])result);
        }
        
    }
    
    AlarmDashlet(Dashboard dashboard) {
        super(dashboard, "Alarms");
        setLoader(m_loader);
        setView(m_view);
    }
    
    public void setSurveillanceSet(SurveillanceSet set) {
        m_loader.load(set);
    }

    
    public void setSurveillanceService(SurveillanceServiceAsync svc) {
        m_loader.setSurveillanceService(svc);
    }

}
