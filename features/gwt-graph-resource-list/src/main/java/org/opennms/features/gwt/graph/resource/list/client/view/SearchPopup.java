/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.graph.resource.list.client.view;

import org.opennms.features.gwt.graph.resource.list.client.event.SearchClickEvent;
import org.opennms.features.gwt.graph.resource.list.client.event.SearchClickEventHandler;
import org.opennms.features.gwt.graph.resource.list.client.presenter.DefaultResourceListPresenter.SearchPopupDisplay;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class SearchPopup extends PopupPanel implements SearchPopupDisplay {
    
    private Label m_label;
    private TextBox m_tf;
    private Button m_okBtn;
    private Button m_cancelBtn;
    private SimpleEventBus m_eventBus = new SimpleEventBus();
    private UIObject m_target;
    private LayoutPanel m_layoutPanel;
    private int m_heightOffset = 274;
    
    public SearchPopup() {
        super(true);
        
        m_label = new Label("Search for Node:");
        m_label.getElement().getStyle().setFontSize(70, Unit.PCT);
        m_label.getElement().getStyle().setPaddingTop(4, Unit.PX);
        m_label.getElement().getStyle().setPaddingLeft(5, Unit.PX);
        m_tf = new TextBox();
        m_tf.setSize("99%", "15px");
        m_okBtn = new Button("OK");
        m_okBtn.setSize("100%", "100%");
        m_okBtn.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                m_eventBus.fireEvent(new SearchClickEvent(m_tf.getText()));
                hide();
            }
        });
        
        m_cancelBtn = new Button("Cancel");
        m_cancelBtn.setSize("100%", "100%");
        m_cancelBtn.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        m_layoutPanel = new LayoutPanel();
        m_layoutPanel.setSize("100%", "25px");
        m_layoutPanel.add(m_label);
        m_layoutPanel.add(m_tf);
        m_layoutPanel.add(m_okBtn);
        m_layoutPanel.add(m_cancelBtn);
        m_layoutPanel.setWidgetRightWidth(m_cancelBtn, 0, Unit.PX, 60, Unit.PX);
        m_layoutPanel.setWidgetRightWidth(m_okBtn, 64, Unit.PX, 70, Unit.PX);
        m_layoutPanel.setWidgetLeftRight(m_tf, 100, Unit.PX, 135, Unit.PX);
        
        setAnimationEnabled(true);
        
        setWidget(m_layoutPanel);
    }
    
    public void addSearchClickEventHandler(SearchClickEventHandler handler) {
        m_eventBus.addHandler(SearchClickEvent.getType(), handler);
    }

    @Override
    public HasClickHandlers getSearchConfirmBtn() {
        return m_okBtn;
    }

    @Override
    public HasClickHandlers getCancelBtn() {
        return m_cancelBtn;
    }

    @Override
    public String getSearchText() {
        return m_tf.getText();
    }

    @Override
    public void showSearchPopup() {
        
        setPopupPositionAndShow(new PositionCallback() {
            
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = m_target.getAbsoluteLeft();
                int top = m_target.getAbsoluteTop() + getHeightOffset();
                m_layoutPanel.setWidth((m_target.getOffsetWidth() - 12) + "px");
                setPopupPosition(left, top);
                
            }
        });
        
    }

    @Override
    public void hideSearchPopup() {
        hide();
    }

    @Override
    public void setTargetWidget(Widget target) {
        m_target = target;
    }

    @Override
    public HasKeyPressHandlers getTextBox() {
        return m_tf;
    }

    @Override
    public void setHeightOffset(int offset) {
        m_heightOffset = offset;
        
    }
    
    private int getHeightOffset() {
        return m_heightOffset;
    }
    
    

}
