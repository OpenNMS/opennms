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

package org.opennms.features.topology.app.internal.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import org.opennms.features.topology.app.internal.gwt.client.SearchSuggestion;

public class SearchTokenField extends Composite {

    public interface RemoveCallback{
        void onRemove(SearchSuggestion searchSuggestion);
    }

    public interface CenterOnSuggestionCallback{
        void onCenter(SearchSuggestion searchSuggestion);
    }

    private static SearchTokenFieldUiBinder uiBinder = GWT.create(SearchTokenFieldUiBinder.class);
    public interface SearchTokenFieldUiBinder extends UiBinder<Widget, SearchTokenField>{}

    @UiField
    FlowPanel m_namespace;

    @UiField
    FlowPanel m_label;

    @UiField
    Anchor m_closeBtn;

    @UiField
    Anchor m_centerSuggestionBtn;

    @UiField
    HorizontalPanel m_tokenContainer;

    private SearchSuggestion m_suggestion;
    private RemoveCallback m_removeCallback;
    private CenterOnSuggestionCallback m_centerOnCallback;

    public SearchTokenField(SearchSuggestion searchSuggestion) {
        initWidget(uiBinder.createAndBindUi(this));
        m_suggestion = searchSuggestion;
        init();
    }

    @Override
    protected void onLoad() {
        super.onLoad();

    }

    private void init() {
        m_tokenContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        m_closeBtn.setTitle("Remove from focus");
        m_closeBtn.getElement().getStyle().setCursor(Style.Cursor.POINTER);

        m_centerSuggestionBtn.setTitle("Center On Map");
        m_centerSuggestionBtn.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        setLabel(m_suggestion.getLabel());
        setNamespace(m_suggestion.getNamespace());

    }

    public void setRemoveCallback(RemoveCallback callback) {
        m_removeCallback = callback;
    }

    public void setCenterOnCallback(CenterOnSuggestionCallback callback){
        m_centerOnCallback = callback;
    }

    public void setNamespace(String namespace) {
        m_namespace.getElement().setInnerText(namespace + ": ");
    }

    public void setLabel(String label) {
        m_label.getElement().setInnerText(label);
    }

    @UiHandler("m_closeBtn")
    void handleClick(ClickEvent event) {
        if (m_removeCallback != null) {
            m_removeCallback.onRemove(m_suggestion);
        }
    }

    @UiHandler("m_centerSuggestionBtn")
    void handleCenterOnClick(ClickEvent event){
        if(m_centerOnCallback != null){
            m_centerOnCallback.onCenter(m_suggestion);
        }
    }

}
