/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

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