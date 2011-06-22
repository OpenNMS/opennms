/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.reports.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchPopup extends PopupPanel {
    private Label m_label;
    private TextBox m_tf;
    private Button m_okBtn;
    private Button m_cancelBtn;
    private SimpleEventBus m_eventBus = new SimpleEventBus();
    
    public SearchPopup() {
        super(true);
        
        m_label = new Label("Search for Node:");
        m_tf = new TextBox();
        m_okBtn = new Button("OK");
        m_okBtn.addClickHandler(new ClickHandler() {
            
            public void onClick(ClickEvent event) {
                m_eventBus.fireEvent(new SearchClickEvent(m_tf.getText()));
                hide();
            }
        });
        
        m_cancelBtn = new Button("Cancel");
        m_cancelBtn.addClickHandler(new ClickHandler() {
            
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(m_okBtn);
        hPanel.add(m_cancelBtn);
        
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(m_label);
        vPanel.add(m_tf);
        vPanel.add(hPanel);
        
        setWidget(vPanel);
    }
    
    public void addSearchClickEventHandler(SearchClickEventHandler handler) {
        m_eventBus.addHandler(SearchClickEvent.getType(), handler);
    }
    
    

}
