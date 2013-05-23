/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

/**
 * <p>Abstract Dashlet class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class Dashlet extends Composite {
    
    class DashletTitle extends Composite {
        private DockPanel m_panel = new DockPanel();
        private Label m_label = new Label();
        
        DashletTitle(String title, DashletLoader loader) {
            
            m_label.setText(title);
            

            m_label.addStyleName("dashletTitle");
            m_panel.addStyleName("dashletTitlePanel");
            m_panel.add(m_label, DockPanel.WEST);
            m_panel.add(m_loader, DockPanel.EAST);

            m_panel.setCellVerticalAlignment(m_loader, DockPanel.ALIGN_MIDDLE);
            m_panel.setCellHorizontalAlignment(m_loader, DockPanel.ALIGN_RIGHT);

            initWidget(m_panel);
        }
        
        @Override
        public void setTitle(String title) {
            m_label.setText(title);
        }
        
        public void add(Widget widget, DockLayoutConstant constraint) {
            m_panel.add(widget, constraint);
        }
        
    }
    
    private VerticalPanel m_panel = new VerticalPanel();
    private String m_title;
    private DashletTitle m_titleWidget;
    private DashletView m_view;
    private DashletLoader m_loader;
    private Dashboard m_dashboard;

    /**
     * <p>Constructor for Dashlet.</p>
     *
     * @param dashboard a {@link org.opennms.dashboard.client.Dashboard} object.
     * @param title a {@link java.lang.String} object.
     */
    public Dashlet(Dashboard dashboard, String title) {
        m_title = title;
        m_dashboard = dashboard;
        initWidget(m_panel);
    }

    /**
     * <p>setView</p>
     *
     * @param view a {@link org.opennms.dashboard.client.DashletView} object.
     */
    protected void setView(DashletView view) {
        m_view = view;
    }
    
    /**
     * <p>setView</p>
     *
     * @param view a {@link com.google.gwt.user.client.ui.Widget} object.
     */
    protected void setView(Widget view) {
        setView(new DashletView(this, view));
    }
    
    /**
     * <p>getTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTitle() {
        return m_title;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTitle(String title) {
        m_title = title;
        m_titleWidget.setTitle(m_title);
    }
    
    /**
     * <p>addToTitleBar</p>
     *
     * @param widget a {@link com.google.gwt.user.client.ui.Widget} object.
     * @param constraint a {@link com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant} object.
     */
    public void addToTitleBar(Widget widget, DockLayoutConstant constraint) {
        m_titleWidget.add(widget, constraint);
    }
    
    /**
     * <p>setLoader</p>
     *
     * @param loader a {@link org.opennms.dashboard.client.DashletLoader} object.
     */
    public void setLoader(DashletLoader loader) {
        m_loader = loader;
    }

    /**
     * <p>onLoad</p>
     */
    @Override
    protected void onLoad() {
        if (m_loader == null) {
            m_loader = new DashletLoader();
        }
        m_titleWidget = new DashletTitle(m_title, m_loader);
        
        m_panel.setStyleName("dashletPanel");
        
        m_panel.add(m_titleWidget);
        m_panel.add(m_view);
        
        m_view.onDashLoad();
        
    }
    
    /**
     * <p>error</p>
     *
     * @param caught a {@link java.lang.Throwable} object.
     */
    protected void error(Throwable caught) {
        m_dashboard.error(caught);
    }

    /**
     * <p>error</p>
     *
     * @param err a {@link java.lang.String} object.
     */
    public void error(String err) {
        m_dashboard.error(err);
    }
    

	/**
	 * <p>setSurveillanceSet</p>
	 *
	 * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
	 */
	public void setSurveillanceSet(SurveillanceSet set) {
		// TODO Auto-generated method stub
		
	}
    
    
    
    

}
