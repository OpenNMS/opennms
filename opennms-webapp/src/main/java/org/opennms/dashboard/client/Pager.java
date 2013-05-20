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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>Pager class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class Pager extends Composite {
    
    private class PageControl extends Composite {
        
        Label m_label;
        int m_direction;
        
        PageControl(String text, int direction) {
            m_label = new Label(text);
            m_label.addStyleName(direction > 0 ? "pagerRight" : "pagerLeft");
            m_direction = direction;
            
            m_label.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent sender) {
                    adjustPage(m_direction);
                    
                }
                
            });
            initWidget(m_label);
        }


    }
    

    private DockPanel m_panel = new DockPanel();
    private Label m_label = new Label();
    private Pageable m_pageable;
    
    /**
     * <p>Constructor for Pager.</p>
     *
     * @param pageable a {@link org.opennms.dashboard.client.Pageable} object.
     */
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

    /**
     * <p>update</p>
     */
    public void update() {
        updateLabel();
    }

}
