package org.opennms.dashboard.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

public class SurveillanceDashlet extends Dashlet {
    
    private SurveillanceListenerCollection m_listeners = new SurveillanceListenerCollection();
    private SurveillanceData m_data;

    private SurveillanceView m_view;
    private SurveillanceLoader m_loader;
    
    class SurveillanceLoader extends DashletLoader implements AsyncCallback {
        
        private SurveillanceServiceAsync m_surveillanceService;
        
        protected void onLoad() {
            load();
        }

        public void load() {
            loading();
            m_surveillanceService.getSurveillanceData(this);
        }
        
        public void onFailure(Throwable caught) {
            loadError(caught);
            error(caught);
        }

        public void onSuccess(Object result) {
            SurveillanceData data = (SurveillanceData)result;
            setData(data);
            
            
            if (!data.isComplete()) {
                final AsyncCallback cb = this;
                Timer timer = new Timer() {
                    public void run() {
                        m_surveillanceService.getSurveillanceData(cb);
                    }
                };
                timer.schedule(2000);
            } else {
                complete();
            }
        }

        public void setSurveillanceService(SurveillanceServiceAsync surveillanceService) {
            m_surveillanceService = surveillanceService;
        }
        
    }

    
    class SurveillanceView extends DashletView {
        
        private Grid m_grid = new Grid();
        
        public SurveillanceView() {
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
            
            initWidget(m_grid);
            
        }
        
        void populate(SurveillanceData data) {
            m_grid.resize(data.getRowCount()+1, data.getColumnCount()+1);
            
            // set row 0 to be column headings
            m_grid.getRowFormatter().setStyleName(0, "header");
            m_grid.setText(0, 0, "Show all nodes");
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
            
        }

        
    }
    
    
    public SurveillanceDashlet(Dashboard dashboard) {
        super(dashboard, "Surveillance View");
        m_view = new SurveillanceView();
        m_loader = new SurveillanceLoader();

        setLoader(m_loader);
        setView(m_view);

    }
    
    public void setData(SurveillanceData data) {
        m_data = data;
        m_view.populate(data);
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


    void initialLoader(String serviceEntryPoint) {
        m_loader.load();
    }

    public void setSurveillanceService(SurveillanceServiceAsync svc) {
        m_loader.setSurveillanceService(svc);
    }

}
