/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.dashboard.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * <p>DashletLoader class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DashletLoader extends Composite {
    
    /** Constant <code>COMPLETE=0</code> */
    public static final int COMPLETE = 0;
    /** Constant <code>LOADING=1</code> */
    public static final int LOADING = 1;
    /** Constant <code>ERROR=2</code> */
    public static final int ERROR = 2;

    SimplePanel m_panel = new SimplePanel();
    Image m_progressIcon = new Image(GWT.getHostPageBaseURL()+"images/progress.gif");
    Image m_errorIcon = new Image(GWT.getHostPageBaseURL()+"images/error.png");
    
    DashletLoader() {
        m_panel.addStyleName("dashletLoader");
        initWidget(m_panel);
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a int.
     * @param description a {@link java.lang.String} object.
     */
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
    
    /**
     * <p>loading</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public void loading(String msg) {
        setStatus(LOADING, msg);
    }
    
    /**
     * <p>loading</p>
     */
    public void loading() {
        loading("Loading...");
    }
    
    /**
     * <p>loadError</p>
     *
     * @param caught a {@link java.lang.Throwable} object.
     */
    public void loadError(Throwable caught) {
        setStatus(ERROR, "Error");
    }
    
    /**
     * <p>complete</p>
     */
    public void complete() {
        setStatus(COMPLETE, "");
    }
    
}
