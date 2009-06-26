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
 * Created: February 22, 2007
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
public class Pager extends Composite {
    
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
                    adjustPage(m_direction);
                    
                }
                
            });
            initWidget(m_label);
        }


    }
    

    private DockPanel m_panel = new DockPanel();
    private Label m_label = new Label();
    private Pageable m_pageable;
    
    public Pager(Pageable pageable) {
        
        m_pageable = pageable;
        m_panel.addStyleName("pager");
        m_panel.add(createLeftPageControl(), DockPanel.WEST);
        m_label.addStyleName("pagerText");
        m_panel.add(m_label, DockPanel.CENTER);
        m_panel.add(createRightPageControl(), DockPanel.EAST);
        
        initWidget(m_panel);
        
        updateLabel();
    }
    
    private void updateLabel() {
        int current = m_pageable.getCurrentElement();
        int pageSize = m_pageable.getPageSize();
        int total = m_pageable.getElementCount();
        
        int first = current+1;
        int last = Math.min(current+pageSize, total);
                
        m_label.setText(first+" to "+last+" of "+total);

    }

    private Widget createRightPageControl() {
        return new PageControl(">>", 1);
    }

    private Widget createLeftPageControl() {
        return new PageControl("<<", -1);
    }
    
    private int getPageSize() {
        return Math.max(1, m_pageable.getPageSize());
    }
    
    /**
     * This index is the maximum index that can be set paged on page size.
     * 
     * So for a 5 item page with 7 total elements then the max index is 5
     * with will show the last two items on the last page (maxIndex must be a multiple of page size)
     *
     * if max elements is a multiple of page size it ends up as max - pageSize
     * @return
     */
    private int getMaxIndex() {
        
        int pages = m_pageable.getElementCount() / getPageSize();
        int extras = m_pageable.getElementCount() % getPageSize();

        if (extras == 0) {
            return (pages - 1) * getPageSize();
        } else {
            return pages * getPageSize();
        }

    }
    
    private void adjustPage(int direction) {
        
        int adjustment = direction * getPageSize();
        int maxIndex = getMaxIndex();

        int newIndex = Math.max(Math.min(m_pageable.getCurrentElement()+adjustment, maxIndex), 0);

        m_pageable.setCurrentElement(newIndex);

    }

    public void update() {
        updateLabel();
    }

}
