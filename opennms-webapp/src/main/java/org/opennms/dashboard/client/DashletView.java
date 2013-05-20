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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant;

/**
 * <p>DashletView class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DashletView extends Composite {
    
    private Dashlet m_dashlet;

    /**
     * <p>Constructor for DashletView.</p>
     *
     * @param dashlet a {@link org.opennms.dashboard.client.Dashlet} object.
     */
    protected DashletView(Dashlet dashlet) {
        m_dashlet = dashlet;
    }

    /**
     * <p>Constructor for DashletView.</p>
     *
     * @param dashlet a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param view a {@link com.google.gwt.user.client.ui.Widget} object.
     */
    public DashletView(Dashlet dashlet, Widget view) {
        this(dashlet);
        initWidget(view);
    }
    
    /**
     * <p>getTitle</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTitle() {
        return m_dashlet.getTitle();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTitle(String title) {
        m_dashlet.setTitle(title);
    }
    
    /**
     * <p>addToTitleBar</p>
     *
     * @param widget a {@link com.google.gwt.user.client.ui.Widget} object.
     * @param constraint a {@link com.google.gwt.user.client.ui.DockPanel.DockLayoutConstant} object.
     */
    public void addToTitleBar(Widget widget, DockLayoutConstant constraint) {
        m_dashlet.addToTitleBar(widget, constraint);
    }

    /**
     * <p>onDashLoad</p>
     */
    public void onDashLoad() {
        
    }

}
