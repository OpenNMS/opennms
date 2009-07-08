/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SimplePager extends Composite {
    private SimplePageable m_pageable;
    private DockPanel m_pager = new DockPanel();
    
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
        ClickListenerCollection m_listeners = new ClickListenerCollection();
        
        Label m_label;
        int m_direction;
        
        PageControl(String text, int direction) {
            m_label = new Label(text);
            m_label.addStyleName(direction > 0 ? "pagerRight" : "pagerLeft");
            m_direction = direction;
            
            m_label.addClickListener(new ClickListener() {

                public void onClick(Widget sender) {
                    m_pageable.adjustPage(m_direction);
                }
                
            });
            initWidget(m_label);
        }
    }
}