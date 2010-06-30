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
 * Created: February 26, 2007
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>SimplePager class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SimplePager extends Composite {
    private SimplePageable m_pageable;
    private DockPanel m_pager = new DockPanel();
    
    /**
     * <p>Constructor for SimplePager.</p>
     *
     * @param pageable a {@link org.opennms.dashboard.client.SimplePageable} object.
     */
    public SimplePager(SimplePageable pageable) {
        m_pageable = pageable;
        
        m_pager.addStyleName("pager");
        m_pager.add(createLeftPageControl(), DockPanel.WEST);
        //m_pager.add(m_label, DockPanel.CENTER);
        m_pager.add(createRightPageControl(), DockPanel.EAST);
        
        initWidget(m_pager);
    }

    private Widget createRightPageControl() {
        return new PageControl(">>", 1);
    }

    private Widget createLeftPageControl() {
        return new PageControl("<<", -1);
    }
    
    private class PageControl extends Composite {
        Label m_label;
        int m_direction;
        
        PageControl(String text, int direction) {
            m_label = new Label(text);
            m_label.addStyleName(direction > 0 ? "pagerRight" : "pagerLeft");
            m_direction = direction;
            
            m_label.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent sender) {
                    m_pageable.adjustPage(m_direction);
                }
                
            });
            initWidget(m_label);
        }
    }
}
