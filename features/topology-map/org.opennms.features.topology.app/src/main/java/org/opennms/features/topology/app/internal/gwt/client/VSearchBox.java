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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.builder.client.DomDivBuilder;
import com.google.gwt.dom.builder.shared.DivBuilder;
import com.google.gwt.dom.builder.shared.HtmlDivBuilder;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.vaadin.client.ui.VFilterSelect;
import org.opennms.features.topology.app.internal.gwt.client.ui.SearchTokenField;
import org.opennms.features.topology.app.internal.gwt.client.ui.SuggestionMenu;
import org.opennms.features.topology.app.internal.gwt.client.ui.SuggestionMenuItem;

import java.util.*;

public class VSearchBox extends Composite implements SelectionHandler<SuggestOracle.Suggestion>,KeyUpHandler {


    public class CustomDisplay extends SuggestBox.SuggestionDisplay{

        private final SuggestionMenu m_suggestionMenu;

        private final PopupPanel m_suggestionPopup;
        public CustomDisplay() {
            m_suggestionMenu = new SuggestionMenu();
            m_suggestionPopup = createPopup();
        }

        protected PopupPanel createPopup() {
            PopupPanel p = new PopupPanel(true, false);
            p.setStyleName("gwt-SuggestBoxPopup");
            p.setPreviewingAllNativeEvents(true);
            //p.setAnimationType(PopupPanel.AnimationType.ROLL_DOWN);
            return p;
        }

        @Override
        protected SuggestOracle.Suggestion getCurrentSelection() {
            if (!isSuggestionListShowing()) {
                return null;
            }
            SuggestionMenuItem item = m_suggestionMenu.getSelectedItem();
            return item == null ? null : item.getSuggestion();
        }


        @Override
        protected void hideSuggestions() {
            m_suggestionPopup.hide();
        }

        @Override
        protected void moveSelectionDown() {
            if (isSuggestionListShowing()) {
                m_suggestionMenu.selectItem(m_suggestionMenu.getSelectedItemIndex() + 1);
            }
        }

        private boolean isSuggestionListShowing() {
            return m_suggestionPopup.isShowing();
        }

        @Override
        protected void moveSelectionUp() {
            if (isSuggestionListShowing()) {

                if (m_suggestionMenu.getSelectedItemIndex() == -1) {
                    m_suggestionMenu.selectItem(m_suggestionMenu.getNumItems() - 1);
                } else {
                    m_suggestionMenu.selectItem(m_suggestionMenu.getSelectedItemIndex() - 1);
                }
            }
        }

        @Override
        protected void showSuggestions(SuggestBox suggestBox, Collection<? extends SuggestOracle.Suggestion> suggestions, boolean isDisplayStringHTML, boolean isAutoSelectEnabled, final SuggestBox.SuggestionCallback callback) {
            boolean anySuggestions = (suggestions != null && suggestions.size() > 0);

            boolean hideWhenEmpty = true;
            if (!anySuggestions && hideWhenEmpty) {
                hideSuggestions();
                return;
            }

            if (m_suggestionPopup.isAttached()) {
                m_suggestionPopup.hide();
            }

            m_suggestionMenu.clearItems();

            for(final SuggestOracle.Suggestion curSuggestion : suggestions) {
                Scheduler.ScheduledCommand command = new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                       callback.onSuggestionSelected(curSuggestion);
                    }
                };

                final SuggestionMenuItem menuitem = new SuggestionMenuItem(curSuggestion, isDisplayStringHTML, command);

                m_suggestionMenu.addItem(menuitem);
            }

            m_suggestionPopup.addAutoHidePartner(suggestBox.getElement());

            // Show the popup under the TextBox.
            m_suggestionPopup.showRelativeTo(suggestBox);

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

    VerticalPanel m_selectionContainer;
    VerticalPanel m_focusedContainer;

    SuggestBox m_suggestBox;
    public VSearchBox(){
        initWidget(uiBinder.createAndBindUi(this));

    }

    @Override
    public void onLoad(){
        this.setStyleName("topology-search");
        TextBoxBase textField = new TextBox();
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
                m_connector.selectSuggestion(Arrays.asList(selectedItem));
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

        m_selectionContainer = new VerticalPanel();
        m_componentHolder.add(m_selectionContainer);

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


    public void setSelected(List<SearchSuggestion> selected) {
        m_selectionContainer.clear();
        for (SearchSuggestion searchSuggestion : selected) {
            SearchTokenField field = new SearchTokenField(searchSuggestion);
            field.setRemoveCallback(new SearchTokenField.RemoveCallback() {
                @Override
                public void onRemove(SearchSuggestion searchSuggestion) {
                    m_connector.removeSelected(searchSuggestion);
                }
            });
            m_selectionContainer.add(field);
        }
    }

    public void setFocused(List<SearchSuggestion> focused) {
        m_focusedContainer.clear();
        for(SearchSuggestion searchSuggestion : focused){
            SearchTokenField field = new SearchTokenField(searchSuggestion);
            field.setRemoveCallback(new SearchTokenField.RemoveCallback() {
                @Override
                public void onRemove(SearchSuggestion searchSuggestion) {
                    m_connector.removeFocused(searchSuggestion);
                }
            });
            m_focusedContainer.add(field);
        }

    }

    private static native void log(Object message) /*-{
        $wnd.console.debug(message);
    }-*/;

}
