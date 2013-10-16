/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.opennms.features.topology.app.internal.gwt.client.ui.SearchTokenField;

import java.util.*;

public class VSearchBox extends Composite implements SelectionHandler<SuggestOracle.Suggestion>,KeyUpHandler {

    public class DefaultSelectionCallback implements SearchTokenField.SelectionCallback {

        @Override
        public void onSelection(SearchSuggestion searchSuggestion) {
            m_connector.selectSuggestion(searchSuggestion);
        }

        @Override
        public void onDeselection(SearchSuggestion searchSuggestion) {
            m_connector.removeSelected(searchSuggestion);
        }
    }

    public class DefaultCenterOnCallback implements SearchTokenField.CenterOnSuggestionCallback{

        @Override
        public void onCenter(SearchSuggestion searchSuggestion) {
            m_connector.centerOnSuggestion(searchSuggestion);
        }
    }

    public class RemoteSuggestOracle extends SuggestOracle{

        RemoteSuggestOracle(){
        }

        @Override
        public boolean isDisplayStringHTML() {
            return true;
        }

        @Override
        public void requestSuggestions(Request request, Callback callback) {
            m_connector.query(request, callback, m_indexFrom, m_indexTo);
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

    VerticalPanel m_focusedContainer;

    SuggestBox m_suggestBox;
    public VSearchBox(){
        initWidget(uiBinder.createAndBindUi(this));

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

        m_focusedContainer = new VerticalPanel();
        m_focusedContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        m_focusedContainer.setTitle("Focused Vertices");
        m_componentHolder.add(m_focusedContainer);
    }

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
            field.setSelectionCallback(new DefaultSelectionCallback());

            m_focusedContainer.add(field);
        }

    }

    private static native void log(Object message) /*-{
        $wnd.console.debug(message);
    }-*/;

}
