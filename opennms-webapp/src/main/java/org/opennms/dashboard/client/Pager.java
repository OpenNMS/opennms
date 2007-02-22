package org.opennms.dashboard.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class Pager extends Composite {
    
    private class PageControl extends Composite {
        
        ClickListenerCollection m_listeners = new ClickListenerCollection();
        
        Label m_label;
        int m_direction;
        
        PageControl(String text, int direction) {
            m_label = new Label(text);
            m_direction = direction;
            
            m_label.addClickListener(new ClickListener() {

                public void onClick(Widget sender) {
                    Window.alert("Clicked with direction "+m_direction);
                    adjustPage(m_direction);
                    
                }
                
            });
            initWidget(m_label);
        }


    }
    

    private DockPanel m_panel = new DockPanel();
    private Label m_label = new Label();;
    private Pageable m_pageable;
    
    public Pager(Pageable pageable) {
        
        m_pageable = pageable;
        m_panel.add(createLeftPageControl(), DockPanel.WEST);
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

        Window.alert("Setting new index to "+newIndex);
        m_pageable.setCurrentElement(newIndex);

    }

    public void update() {
        updateLabel();
    }

}
