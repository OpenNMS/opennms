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

package org.opennms.features.topology.app.internal.gwt.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.Collection;

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
