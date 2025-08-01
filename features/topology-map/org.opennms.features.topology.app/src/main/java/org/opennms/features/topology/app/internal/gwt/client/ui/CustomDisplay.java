/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal.gwt.client.ui;

import java.util.Collection;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;

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

    @Override
    public boolean isSuggestionListShowing() {
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
