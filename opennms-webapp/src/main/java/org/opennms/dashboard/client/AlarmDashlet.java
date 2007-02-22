package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
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
            setStatus("Loading...");
            m_suveillanceService.getAlarmsForSet(surveillanceSet, this);
        }
        
        public void onDataLoaded(Alarm[] alarms) {
            m_view.setAlarms(alarms);
            setStatus("");
        }

        public void setSurveillanceService(SurveillanceServiceAsync svc) {
            m_suveillanceService = svc;
        }

        public void onFailure(Throwable caught) {
            setStatus("Error");
            error(caught);
        }

        public void onSuccess(Object result) {
            onDataLoaded((Alarm[])result);
        }
        
    }
    
    class AlarmView extends DashletView {
        
        FlexTable m_alarmTable = new FlexTable();
        
        int m_rows = 5;
        
        AlarmView() {
            initializeTable();
            initWidget(m_alarmTable);
        }
        
        private void initializeTable() {
            m_alarmTable.setText(0, 0, "Node");
            m_alarmTable.setText(0, 1, "Description");
            m_alarmTable.setText(0, 2, "Count");
            m_alarmTable.getRowFormatter().addStyleName(0, "header");
            
            for(int i = 1; i <= m_rows; i++) {
                clearRow(i);
            }
        }

        private void clearRow(int row) {
            m_alarmTable.setText(row, 0, "");
            m_alarmTable.setText(row, 1, "");
            m_alarmTable.setText(row, 2, "");
            m_alarmTable.getRowFormatter().addStyleName(0, "empty");
        }

        public void setAlarms(Alarm[] alarms) {
            int rows = Math.min(m_rows, alarms.length);
            
            for(int i = 1; i <= rows; i++) {
                setRow(i, alarms[i-1]);
            }
            
            for(int i = rows+1; i <= m_rows; i++) {
                clearRow(i);
            }

        }

        private void setRow(int row, Alarm alarm) {
            m_alarmTable.setText(row, 0, alarm.getNodeLabel());
            m_alarmTable.setText(row, 1, alarm.getDescrption());
            m_alarmTable.setText(row, 2, ""+alarm.getCount());
            m_alarmTable.getRowFormatter().addStyleName(row, alarm.getSeverity());
        }

        public void setRows(int rows) {
            m_rows = rows;
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
