/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.dashboard.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DashletLoader extends Composite {
    
    public static final int COMPLETE = 0;
    public static final int LOADING = 1;
    public static final int ERROR = 2;

    SimplePanel m_panel = new SimplePanel();
    Image m_progressIcon = new Image(GWT.getModuleBaseURL()+"images/progress.gif");
    Image m_errorIcon = new Image(GWT.getModuleBaseURL()+"images/error.png");
    
    DashletLoader() {
        m_panel.addStyleName("dashletLoader");
        initWidget(m_panel);
    }

    public void setStatus(int status, String description) {
        switch( status ) {
        case ERROR:
            m_errorIcon.setTitle(description);
            m_panel.setWidget(m_errorIcon);
            break;
        case LOADING:
            m_progressIcon.setTitle(description);
            m_panel.setWidget(m_progressIcon);
            break;
        case COMPLETE:
            if (m_panel.getWidget() != null) {
              m_panel.remove(m_panel.getWidget());
            }
            break;
        }
    }
    
    public void loading(String msg) {
        setStatus(LOADING, msg);
    }
    
    public void loading() {
        loading("Loading...");
    }
    
    public void loadError(Throwable caught) {
        setStatus(ERROR, "Error");
    }
    
    public void complete() {
        setStatus(COMPLETE, "");
    }
    
}
