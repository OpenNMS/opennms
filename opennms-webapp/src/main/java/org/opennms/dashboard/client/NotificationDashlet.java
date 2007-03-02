package org.opennms.dashboard.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class NotificationDashlet extends Dashlet {
    
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
    
    private NotificationView m_view = new NotificationView();
    private NotificationLoader m_loader = new NotificationLoader();
    
    class NotificationLoader extends DashletLoader implements AsyncCallback {
        
        private SurveillanceServiceAsync m_suveillanceService;
        
        public void load(final SurveillanceSet surveillanceSet) {
            try {
                loading();
                m_suveillanceService.getNotificationsForSet(surveillanceSet, this);
            } catch (Exception e) {
                onFailure(e);
            }
        }
        
        public void onDataLoaded(Notification[] notifications) {
            try {
                m_view.setNotifications(notifications);
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
            onDataLoaded((Notification[])result);
        }
        
    }
    
    NotificationDashlet(Dashboard dashboard) {
        super(dashboard, "Notifications");
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
