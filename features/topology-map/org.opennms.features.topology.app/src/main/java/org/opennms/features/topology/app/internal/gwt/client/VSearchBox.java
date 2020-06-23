/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client;

import java.util.List;
import java.util.Map;

import org.opennms.features.topology.app.internal.gwt.client.ui.SearchTokenField;
import org.opennms.features.topology.app.internal.gwt.client.ui.SearchTokenField.CollapseCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VSearchBox extends Composite implements SelectionHandler<SuggestOracle.Suggestion>,KeyUpHandler {

    private HandlerRegistration m_windowResizeRegistration;

    public class DefaultCenterOnCallback implements SearchTokenField.CenterOnSuggestionCallback{

        @Override
        public void onCenter(SearchSuggestion searchSuggestion) {
            m_connector.centerOnSuggestion(searchSuggestion);
        }
    }

    public class RemoteSuggestOracle extends SuggestOracle {
        
        private Timer m_keyTimer = new Timer() {
            @Override
            public void run() {
            }
        };
        
        RemoteSuggestOracle() {
        }

        @Override
        public boolean isDisplayStringHTML() {
            return true;
        }

        @Override
        public void requestSuggestions(final Request request, final Callback callback) {

            m_keyTimer.cancel();
            
            m_keyTimer = new Timer() {
                @Override
                public void run() {
                    m_connector.query(request, callback, m_indexFrom, m_indexTo);

                }
            };
            
            m_keyTimer.schedule(500);
            
        }
    }

    private Map<String, String> m_valueMap;

    private int m_indexFrom = 0;
    private int m_indexTo = 0;

    private boolean m_isMultiValued = true;
    private SearchBoxConnector m_connector;
    private static VSearchComboBoxUiBinder uiBinder = GWT.create(VSearchComboBoxUiBinder.class);
    interface VSearchComboBoxUiBinder extends UiBinder<Widget, VSearchBox> {}
    @UiField
    FlowPanel m_componentHolder;

    FlowPanel m_scrollContainer;
    VerticalPanel m_focusedContainer;

    SuggestBox m_suggestBox;
    public VSearchBox(){
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        m_windowResizeRegistration.removeHandler();
    }

    @Override
    public void onLoad(){
        m_componentHolder.clear();
        this.setStyleName("topology-search");
        final TextBoxBase textField = new TextBox();
        textField.setWidth("245px");
        textField.setStyleName("topology-search-box");
        textField.getElement().setAttribute("placeholder", "Search...");
        textField.setFocus(true);
        RemoteSuggestOracle oracle = new RemoteSuggestOracle();

        m_suggestBox = new SuggestBox(oracle, textField);


        m_suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                SearchSuggestion selectedItem = (SearchSuggestion) event.getSelectedItem();
                textField.setText("");
                m_connector.addToFocus(selectedItem);
            }
        });

        if(m_isMultiValued){
            m_suggestBox.setStyleName("multivalue");
        }

        m_suggestBox.addStyleName("wideTextField");
        m_suggestBox.addSelectionHandler(this);
        m_suggestBox.addKeyUpHandler(this);

        m_componentHolder.setWidth("245px");
        m_componentHolder.add(m_suggestBox);


        if(m_focusedContainer == null){
            m_focusedContainer = new VerticalPanel();
            m_scrollContainer = new FlowPanel();
            m_scrollContainer.add(m_focusedContainer);

        }

        m_focusedContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        m_focusedContainer.setTitle("Focused Vertices");
        m_componentHolder.add(m_scrollContainer);

        Timer timer = new Timer(){

            @Override
            public void run(){
                updateScrollPanelSize();
            }
        };

        timer.schedule(1000);

        m_windowResizeRegistration = Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                updateScrollPanelSize();
            }
        });

    }

    private void updateScrollPanelSize() {
        Element topologyComponent = DOM.getElementById("TopologyComponent");
        int topoHeight = topologyComponent.getOffsetHeight();
        int containerHeight = topoHeight - (m_focusedContainer.getElement().getOffsetTop() + 5);
        int tableHeight = m_focusedContainer.getOffsetHeight();

        if(containerHeight >= 0){
            m_scrollContainer.setHeight("" + Math.min(containerHeight, tableHeight) + "px");

            if(tableHeight > containerHeight){
                m_scrollContainer.getElement().getStyle().setOverflowY(Style.Overflow.SCROLL);
            } else{
                m_scrollContainer.getElement().getStyle().setOverflowY(Style.Overflow.HIDDEN);
            }
        }

    }

    public static native void debug(Object obj) /*-{
        $wnd.console.debug(obj);
    }-*/;

    @Override
    public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        SuggestOracle.Suggestion suggestion = event.getSelectedItem();
    }


    @Override
    public void onKeyUp(KeyUpEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSearchBoxConnector(SearchBoxConnector connector) {
        m_connector = connector;
    }

    public void setFocused(List<SearchSuggestion> focused) {
        m_focusedContainer.clear();
        if (focused == null) {
            log("Focus list for searchbox is null");
            updateScrollPanelSize();
            return;
        }
        log(focused);
        for(SearchSuggestion searchSuggestion : focused){
            SearchTokenField field = new SearchTokenField(searchSuggestion);
            field.setRemoveCallback(new SearchTokenField.RemoveCallback() {
                @Override
                public void onRemove(SearchSuggestion searchSuggestion) {
                    m_connector.removeFocused(searchSuggestion);
                }
            });
            field.setCenterOnCallback(new DefaultCenterOnCallback());
            if (searchSuggestion.isCollapsible()) {
                field.setCollapseCallback(new CollapseCallback() {
                    @Override
                    public void onCollapse(SearchSuggestion searchSuggestion) {
                        m_connector.toggleSuggestionCollapse(searchSuggestion);
                        // Update the state of the local object
                        searchSuggestion.setCollapsed(!searchSuggestion.isCollapsed());
                    }
                });
            }

            m_focusedContainer.add(field);
        }

        updateScrollPanelSize();

    }

    private static native void log(Object message) /*-{
        $wnd.console.debug(message);
    }-*/;

}
