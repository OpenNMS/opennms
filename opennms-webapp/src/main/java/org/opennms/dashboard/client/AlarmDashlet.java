package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

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
    
    class AlarmView extends DashletView implements Pageable {
        
        private VerticalPanel m_panel = new VerticalPanel();
        
        private FlexTable m_alarmTable = new FlexTable();
        
        private int m_rows = 5;

        private Alarm[] m_alarms;
        
        private int m_currentIndex = 0;
        
        private Pager m_pager;
        
        AlarmView() {
            initializeTable();
            
            m_pager = new Pager(this);
            
            m_panel.add(m_alarmTable);
            m_panel.add(m_pager);
            initWidget(m_panel);
        }
        
        private void initializeTable() {
            m_alarmTable.setText(0, 0, "Node");
            m_alarmTable.setText(0, 1, "Description");
            m_alarmTable.setText(0, 2, "Count");
            m_alarmTable.getRowFormatter().setStyleName(0, "header");
            
            for(int i = 1; i <= m_rows; i++) {
                clearRow(i);
            }
        }

        private void clearRow(int row) {
            if (row >= m_alarmTable.getRowCount()) {
                return;
            }
            m_alarmTable.clearCell(row, 0);
            m_alarmTable.clearCell(row, 1);
            m_alarmTable.clearCell(row, 2);
            String currStyle = m_alarmTable.getRowFormatter().getStyleName(row);
            if (currStyle != null) {
                m_alarmTable.getRowFormatter().removeStyleName(row, currStyle);
            }
        }

        public void setAlarms(Alarm[] alarms) {
            m_alarms = alarms;
            refresh();
            
        }
        
        private void refresh() {

            int rows = Math.min(m_currentIndex+m_rows, m_alarms.length);
            
            for(int i = m_currentIndex+1; i <= rows; i++) {
                setRow(i - m_currentIndex, m_alarms[i-1]);
            }
            
            for(int i = rows+1; i <= m_currentIndex+m_rows; i++) {
                clearRow(i - m_currentIndex);
            }

            m_pager.update();
        }

        private void setRow(int row, Alarm alarm) {
            m_alarmTable.setText(row, 0, alarm.getNodeLabel());
            m_alarmTable.setText(row, 1, alarm.getDescrption());
            m_alarmTable.setText(row, 2, ""+alarm.getCount());
            m_alarmTable.getRowFormatter().setStyleName(row, alarm.getSeverity());
        }

        public void setRows(int rows) {
            m_rows = rows;
        }

        public int getCurrentElement() {
            return m_currentIndex;
        }

        public int getElementCount() {
            return (m_alarms == null ? 0 : m_alarms.length);
        }

        public int getPageSize() {
            return m_rows;
        }

        public void setCurrentElement(int element) {
            m_currentIndex = element;
            refresh();
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
