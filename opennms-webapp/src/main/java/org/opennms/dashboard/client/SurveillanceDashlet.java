package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SurveillanceDashlet extends Composite {
    
    class DashletTitle extends Composite {
        private DockPanel m_panel = new DockPanel();
        private Label m_label = new Label();
        private Label m_status = new Label();
        DashletTitle(String title) {
            
            m_label.setText(title);
            m_panel.add(m_label, DockPanel.WEST);
            m_panel.add(m_status, DockPanel.EAST);
            initWidget(m_panel);
        }
        
        public void setStatus(String status) {
            m_status.setText(status);
        }
        
        
    }
    
    private VerticalPanel m_panel = new VerticalPanel();
    private Grid m_grid = new Grid();
    private DashletTitle m_title = new DashletTitle("SurveillanceView");
    private SurveillanceListenerCollection m_listeners = new SurveillanceListenerCollection();
    private SurveillanceData m_data;
    
    public SurveillanceDashlet() {
        
        m_panel.add(m_title);
        m_panel.add(m_grid);
        
        m_grid.addTableListener(new TableListener() {

            public void onCellClicked(SourcesTableEvents table, int row, int col) {
                if (row == 0 && col == 0) {
                    onAllClicked();
                } else if (row == 0) {
                    onColumnGroupClicked(col-1);
                } else if (col == 0) {
                    onRowGroupClicked(row-1);
                } else {
                    onIntersectionClicked(row-1, col-1);
                }

            }

        });
        
        initWidget(m_panel);
    }
    
    
    private void onIntersectionClicked(int row, int col) {
        m_listeners.fireIntersectionClicked(this, m_data.getIntersection(row, col));
    }

    private void onColumnGroupClicked(int col) {
        m_listeners.fireSurveillanceGroupClicked(this, m_data.getColumnGroups()[col]);
    }

    private void onRowGroupClicked(int row) {
        m_listeners.fireSurveillanceGroupClicked(this, m_data.getRowGroups()[row]);
    }

    private void onAllClicked() {
        m_listeners.fireAllClicked(this);
    }
    
    public void addSurveillanceViewListener(SurveillanceListener listener) {
        m_listeners.add(listener);
    }
    
    public void removeSurveillanceViewListener(SurveillanceListener listener) {
        m_listeners.remove(listener);
    }

    public void setData(SurveillanceData data) {
        m_data = data;
        populateGrid(data);
    }


    private void populateGrid(SurveillanceData data) {
        m_grid.resize(data.getRowCount()+1, data.getColumnCount()+1);
        
        // set row 0 to be column headings
        for(int col = 0; col < data.getColumnCount(); col++) {
            m_grid.setText(0, col+1, data.getColumnHeading(col));
        }
        
        
        // now do row 1 to rowCount
        for(int row = 0; row < data.getRowCount(); row++) {
            // set the row heading
            m_grid.setText(row+1, 0, data.getRowHeading(row));
            
            // now set the data
            for(int col = 0; col < data.getColumnCount(); col++) {
                m_grid.setText(row+1, col+1, data.getCell(row, col));
            }
        }
        
        if (data.isComplete()) {
            m_title.setStatus("");
        } else {
            m_title.setStatus("Loading");
        }
    }

}
