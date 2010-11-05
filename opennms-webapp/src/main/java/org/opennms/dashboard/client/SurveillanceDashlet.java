/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.dashboard.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

/**
 * <p>SurveillanceDashlet class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceDashlet extends Dashlet {
    
    private SurveillanceListenerCollection m_listeners = new SurveillanceListenerCollection();
    private SurveillanceData m_data;

    private SurveillanceView m_view;
    private SurveillanceLoader m_loader;
    
    class SurveillanceLoader extends DashletLoader implements AsyncCallback<SurveillanceData> {
        
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

        public void onSuccess(SurveillanceData data) {
            setData(data);
            
            
            if (!data.isComplete()) {
                final AsyncCallback<SurveillanceData> cb = this;
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
        
        public SurveillanceView(Dashlet dashlet) {
            super(dashlet);
            m_grid.addTableListener(new TableListener() {

                public void onCellClicked(SourcesTableEvents table, int row, int col) {
                    cellClicked(row, col);
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
        
        protected void cellClicked(int row, int col) {
            clearSelection();
            setSelection(row, col);
        }

        private void setSelection(int row, int col) {
            if (row == 0 && col == 0) {
                // nothing to do just be cleared
            } else if (row == 0) {
                for(int r = 0; r < m_grid.getRowCount(); r++) {
                    m_grid.getCellFormatter().addStyleName(r, col, "selected");
                }
            } else if (col == 0) {
                for(int c = 0; c < m_grid.getColumnCount(); c++) {
                    m_grid.getCellFormatter().addStyleName(row, c, "selected");
                }
            } else {
                m_grid.getCellFormatter().addStyleName(row, col, "selected");
            }
        }

        private void clearSelection() {
            for(int r = 0; r < m_grid.getRowCount(); r++) {
                for(int c = 0; c < m_grid.getColumnCount(); c++) {
                    m_grid.getCellFormatter().removeStyleName(r, c, "selected");
                }
            }
        }

        void populate(SurveillanceData data) {
            setTitle(getTitle()+": "+data.getName());
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
                    SurveillanceIntersection cell = data.getCell(row, col);
                    m_grid.setText(row+1, col+1, cell.getData());
                    m_grid.getCellFormatter().setStyleName(row+1, col+1, cell.getStatus());
                    m_grid.getCellFormatter().addStyleName(row+1, col+1, "divider");
                }
                
                m_grid.getRowFormatter().setStyleName(row+1, "CellStatus");
            }
            
            
            
        }

        
    }
    
    
    /**
     * <p>Constructor for SurveillanceDashlet.</p>
     *
     * @param dashboard a {@link org.opennms.dashboard.client.Dashboard} object.
     */
    public SurveillanceDashlet(Dashboard dashboard) {
        super(dashboard, "Surveillance View");
        m_view = new SurveillanceView(this);
        m_loader = new SurveillanceLoader();

        setLoader(m_loader);
        setView(m_view);

    }
    
    /**
     * <p>setData</p>
     *
     * @param data a {@link org.opennms.dashboard.client.SurveillanceData} object.
     */
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
    
    /**
     * <p>addSurveillanceViewListener</p>
     *
     * @param listener a {@link org.opennms.dashboard.client.SurveillanceListener} object.
     */
    public void addSurveillanceViewListener(SurveillanceListener listener) {
        m_listeners.add(listener);
    }
    
    /**
     * <p>removeSurveillanceViewListener</p>
     *
     * @param listener a {@link org.opennms.dashboard.client.SurveillanceListener} object.
     */
    public void removeSurveillanceViewListener(SurveillanceListener listener) {
        m_listeners.remove(listener);
    }


    void initialLoader(String serviceEntryPoint) {
        m_loader.load();
    }

    /**
     * <p>setSurveillanceService</p>
     *
     * @param svc a {@link org.opennms.dashboard.client.SurveillanceServiceAsync} object.
     */
    public void setSurveillanceService(SurveillanceServiceAsync svc) {
        m_loader.setSurveillanceService(svc);
    }

}
