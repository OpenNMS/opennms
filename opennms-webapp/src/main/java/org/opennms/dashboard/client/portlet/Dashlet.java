/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client.portlet;

import org.opennms.features.dashboard.client.layout.IBasicDBLayout;
import org.opennms.features.dashboard.client.portlet.Portlet;

import com.google.gwt.user.client.ui.Widget;

/**
 * <p>Abstract Dashlet class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class Dashlet extends Portlet{
    private String m_title;
    private DashletView m_view;
    private DashletLoader m_loader;
    private IBasicDBLayout m_dashboard;

    /**
     * <p>Constructor for Dashlet.</p>
     *
     * @param dashboard a {@link org.opennms.dashboard.client.Dashboard} object.
     * @param title a {@link java.lang.String} object.
     */
    public Dashlet(IBasicDBLayout dashboard, String title) {
        super(title);
        m_title = title;
        m_dashboard = dashboard;
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
//    public String getTitle() {
//        return m_title;
//    }
//    
//    /** {@inheritDoc} */
//    public void setTitle(String title) {
//        m_title = title;
//    }
       
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
    protected void onLoad() {
        super.onLoad();
        try {
            if (m_loader == null) {
                m_loader = new DashletLoader();
            }       
            contentpanel.setStyleName("dashletPanel");
            
            contentpanel.add(m_view);
            
            m_view.onDashLoad();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Dashboard.log("Error:"+e.getStackTrace().toString());
        }        
    }
    
    /**
     * <p>
     * error
     * </p>
     * 
     * @param caught
     *            a {@link java.lang.Throwable} object.
     */
    protected void error(Throwable caught) {
        // m_dashboard.error(caught);
    }

    /**
     * <p>
     * error
     * </p>
     * 
     * @param err
     *            a {@link java.lang.String} object.
     */
    public void error(String err) {
        // m_dashboard.error(err);
    }

    public void setSurveillanceSet(SurveillanceSet set) {
        // TODO Auto-generated method stub

    }
}
