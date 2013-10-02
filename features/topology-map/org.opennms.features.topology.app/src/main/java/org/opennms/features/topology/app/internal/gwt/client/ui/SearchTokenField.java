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
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.opennms.features.topology.app.internal.gwt.client.SearchSuggestion;

public class SearchTokenField extends Composite {

    public interface RemoveCallback{
        void onRemove(SearchSuggestion searchSuggestion);
    }


    private static SearchTokenFieldUiBinder uiBinder = GWT.create(SearchTokenFieldUiBinder.class);
    public interface SearchTokenFieldUiBinder extends UiBinder<Widget, SearchTokenField>{}

    @UiField
    SpanElement m_namespace;

    @UiField
    SpanElement m_label;

    @UiField
    Anchor m_closeBtn;

    private SearchSuggestion m_suggestion;
    private RemoveCallback m_removeCallback;

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
        setStyleName("search-token-field");
        setLabel(m_suggestion.getLabel());
        setNamespace(m_suggestion.getNamespace());

    }

    public void setRemoveCallback(RemoveCallback callback) {
        m_removeCallback = callback;
    }

    public void setNamespace(String namespace) {
        m_namespace.setInnerText(namespace);
    }

    public void setLabel(String label) {
        m_label.setInnerText(label);
    }

    @UiHandler("m_closeBtn")
    void handleClick(ClickEvent event) {
        if (m_removeCallback != null) {
            m_removeCallback.onRemove(m_suggestion);
        }
    }
}
