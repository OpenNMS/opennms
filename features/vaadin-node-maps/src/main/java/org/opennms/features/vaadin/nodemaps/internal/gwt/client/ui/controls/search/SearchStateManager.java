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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ComponentTracker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchStringSetEvent;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;

public abstract class SearchStateManager {
    private static final Logger LOG = Logger.getLogger(SearchStateManager.class.getName());

    private SearchState m_state;
    private ValueItem m_valueItem;
    private ValueItem m_history;

    private OpenNMSEventManager m_eventManager;
    private ComponentTracker m_componentTracker;

    public SearchStateManager(final ValueItem valueItem, final ValueItem history, final OpenNMSEventManager eventManager, final ComponentTracker componentTracker) {
        LOG.info("SearchStateManager initializing.");
        m_valueItem = valueItem;
        m_history = history;

        m_eventManager = eventManager;
        m_componentTracker = componentTracker;

        final String valueSearchString = m_valueItem.getValue();
        final String historySearchString = getHistorySearchString();
        if (historySearchString != null) {
            m_valueItem.setValue(historySearchString);
            m_state = State.SEARCHING_FINISHED;
        } else if (valueSearchString != null && !"".equals(valueSearchString)) {
            m_state = State.SEARCHING_FINISHED;
        } else {
            m_state = State.NOT_SEARCHING;
        }
        m_state.initialize(this);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
                m_componentTracker.ready(SearchStateManager.class);
            }
        });
    }

    SearchState getState() {
        return m_state;
    }

    protected ValueItem getValueItem() {
        return m_valueItem;
    }

    public void updateMatchCount(final int matchCount) {
        m_state = m_state.updateMatchCount(this, matchCount);
    }

    public boolean handleAutocompleteEvent(final NativeEvent event) {
        final String eventType = event.getType();
        final int eventKeyCode = event.getKeyCode();
        LOG.info("handleAutocompleteEvent(" + m_state + "): received " + eventType + " (keyCode = " + eventKeyCode + ")");

        if ("keydown".equals(eventType)) {
            switch (eventKeyCode) {
            case KeyCodes.KEY_ESCAPE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.cancelSearching(SearchStateManager.this);
                    }
                });
                event.stopPropagation();
                break;
            case KeyCodes.KEY_ENTER:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.currentEntrySelected(SearchStateManager.this);
                    }
                });
                event.stopPropagation();
                break;
            }
        } else if ("click".equals(eventType) || "touchstart".equals(eventType)) {
            // someone clicked on an entry
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    m_state = m_state.currentEntrySelected(SearchStateManager.this);
                }
            });
            event.stopPropagation();
            return true;
        } else {
            LOG.info("handleAutocompleteEvent(" + m_state + "): unhandled event: " + eventType);
            return true;
        }
        return false;
    }

    public void handleSearchIconEvent(final NativeEvent event) {
        final String eventType = event.getType();
        LOG.info("handleSearchIconEvent(" + m_state + "): received " + eventType + " (keyCode = " + event.getKeyCode() + ")");

        if ("click".equals(eventType) || "touchstart".equals(eventType)) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    m_state = m_state.finishedSearching(SearchStateManager.this);
                }
            });
        } else {
            LOG.info("handleSearchIconEvent(" + m_state + "): unhandled event: " + eventType);
        }
    }

    public void handleInputEvent(final NativeEvent event) {
        final String eventType = event.getType();
        LOG.info("handleInputEvent(" + m_state + "): received " + eventType + " (keyCode = " + event.getKeyCode() + ")");

        if ("keydown".equals(eventType)) {
            switch (event.getKeyCode()) {
            case KeyCodes.KEY_ESCAPE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.cancelSearching(SearchStateManager.this);
                    }
                });
                event.stopPropagation();
                break;
            case KeyCodes.KEY_DOWN:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.goDown(SearchStateManager.this);
                    }
                });
                event.stopPropagation();
                break;
            case KeyCodes.KEY_ENTER:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.finishedSearching(SearchStateManager.this);
                    }
                });
                event.stopPropagation();
                break;
            default:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        final String value = m_valueItem.getValue();
                        if (value == null || "".equals(value)) {
                            m_state = m_state.cancelSearching(SearchStateManager.this);
                        } else {
                            m_state = m_state.searchInputReceived(SearchStateManager.this);
                            sendSearchStringSetEvent(value);
                        }
                    }
                });
                break;
            }
        } else if ("search".equals(eventType) || "change".equals(eventType)) {
            final String searchString = m_valueItem.getValue();
            LOG.info("SearchStateManager.handleInputEvent(): searchString = " + searchString);
            if ("".equals(searchString)) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.cancelSearching(SearchStateManager.this);
                    }
                });
            } else {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        if (searchString == null || "".equals(searchString)) {
                            m_state = m_state.cancelSearching(SearchStateManager.this);
                        } else {
                            m_state = m_state.searchInputReceived(SearchStateManager.this);
                            sendSearchStringSetEvent(searchString);
                        }
                    }
                });
            }
        } else {
            LOG.info("SearchStateManager.handleInputEvent(" + m_state + "): unhandled event: " + eventType);
        }
    }

    protected void sendSearchStringSetEvent(final String value) {
        m_eventManager.fireEvent(new SearchStringSetEvent(value));
    }

    public void reset() {
        m_state = m_state.cancelSearching(this);
    }

    interface SearchState {
        public abstract SearchState initialize(SearchStateManager manager);
        public abstract SearchState cancelSearching(SearchStateManager manager);
        public abstract SearchState finishedSearching(SearchStateManager manager);
        public abstract SearchState currentEntrySelected(SearchStateManager manager);
        public abstract SearchState searchInputReceived(SearchStateManager manager);
        public abstract SearchState goDown(SearchStateManager manager);
        public abstract SearchState goUp(SearchStateManager manager);
        public abstract SearchState updateMatchCount(SearchStateManager manager, int matchCount);
    }

    enum State implements SearchState {
        // Markers are not being filtered, input box is empty, autocomplete is hidden.
        NOT_SEARCHING {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.hideAutocomplete();
                manager.focusInput();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                // make sure input is focused
                manager.clearSearchInput();
                manager.focusInput();
                return this;
            }

            @Override
            public SearchState goDown(final SearchStateManager manager) {
                // if we're not searching, starting navigation won't do anything because
                // we don't have a search phrase yet
                LOG.info("WARNING: attempting to go down, but we're not searching!");
                return this;
            }

            @Override
            public SearchState goUp(final SearchStateManager manager) {
                LOG.info("WARNING: attempting to go up, but we're not searching!");
                return this;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                manager.refresh();
                manager.showAutocomplete();
                return SEARCHING_AUTOCOMPLETE_VISIBLE;
            }

            @Override
            public SearchState finishedSearching(final SearchStateManager manager) {
                // if we're not searching, we can't finish :)
                LOG.info("WARNING: attempting to finish, but we're not searching!");
                return this;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                // if we're not searching, we can't select an entry
                LOG.info("WARNING: attempting to finish, but we're not searching!");
                return this;
            }

            @Override
            public SearchState updateMatchCount(final SearchStateManager manager, final int matchCount) {
                LOG.info("WARNING: match count updated, but we're not searching!");
                return this;
            }
        },
        // Markers are being filtered, input box has content, autocomplete is visible.
        SEARCHING_AUTOCOMPLETE_VISIBLE {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.showAutocomplete();
                manager.focusInput();
                manager.refresh();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                manager.hideAutocomplete();
                manager.clearSearchInput();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.NOT_SEARCHING;
            }

            @Override
            public SearchState goDown(final SearchStateManager manager) {
                // we are already searching with autocomplete visible, but input came
                // to the input box, so we return focus to the autocomplete box
                manager.focusAutocompleteWidget();
                // manager.goDown();
                return SEARCHING_AUTOCOMPLETE_ACTIVE;
            }

            @Override
            public SearchState goUp(final SearchStateManager manager) {
                // We are already searching with autocomplete visible, which means
                // focus is still set to the input box.  Nothing should happen.
                manager.focusAutocompleteWidget();
                return SEARCHING_AUTOCOMPLETE_VISIBLE;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // we're still searching, so just make sure data is up-to-date
                manager.refresh();
                return this;
            }

            @Override
            public SearchState finishedSearching(final SearchStateManager manager) {
                manager.hideAutocomplete();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.SEARCHING_FINISHED;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                // the user has clicked an entry
                manager.entrySelected();
                manager.hideAutocomplete();
                manager.updateHistorySearchString();
                manager.refresh();
                return SEARCHING_FINISHED;
            }

            @Override
            public SearchState updateMatchCount(final SearchStateManager manager, final int matchCount) {
                // if there are no matches, hide the autocomplete
                if (matchCount == 0) {
                    manager.hideAutocomplete();
                    return SEARCHING_AUTOCOMPLETE_HIDDEN;
                } else {
                    return this;
                }
            }
        },
        // Markers are being filtered, input box has content, autocomplete is focused and navigable.
        SEARCHING_AUTOCOMPLETE_ACTIVE {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.showAutocomplete();
                manager.focusAutocompleteWidget();
                manager.refresh();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                manager.hideAutocomplete();
                manager.clearSearchInput();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.NOT_SEARCHING;
            }

            @Override
            public SearchState finishedSearching(final SearchStateManager manager) {
                manager.hideAutocomplete();
                manager.focusInput();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.SEARCHING_FINISHED;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // somehow we've got new search input, user has probably typed something in themselves,
                // flip back to the input-box-has-focus state
                manager.focusInput();
                return SEARCHING_AUTOCOMPLETE_VISIBLE.searchInputReceived(manager);
            }

            @Override
            public SearchState goDown(final SearchStateManager manager) {
                // manager.focusAutocomplete();
                // manager.goDown();
                LOG.warning("Somehow we got a \"down\" event, but the autocomplete listbox should be handling this.  Weird.");
                return this;
            }

            @Override
            public SearchState goUp(final SearchStateManager manager) {
                // manager.focusAutocomplete();
                // manager.goUp();
                LOG.warning("Somehow we got an \"up\" event, but the autocomplete listbox should be handling this.  Weird.");
                return this;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                // the user has clicked an entry or hit enter
                LOG.info("currentEntrySelected()");
                manager.entrySelected();
                manager.hideAutocomplete();
                manager.focusInput();
                manager.updateHistorySearchString();
                manager.refresh();
                return SEARCHING_FINISHED;
            }

            @Override
            public SearchState updateMatchCount(final SearchStateManager manager, final int matchCount) {
                // if there are no matches, hide the autocomplete
                if (matchCount == 0) {
                    manager.hideAutocomplete();
                    return SEARCHING_AUTOCOMPLETE_HIDDEN;
                } else {
                    return this;
                }
            }
        },
        // Markers are being filtered, input box has content, autocomplete is not visible.
        // (this happens when there are 0 matches to the current search)
        SEARCHING_AUTOCOMPLETE_HIDDEN {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.focusInput();
                manager.hideAutocomplete();
                manager.refresh();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                manager.clearSearchInput();
                manager.focusInput();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.NOT_SEARCHING;
            }

            @Override
            public SearchState finishedSearching(final SearchStateManager manager) {
                manager.updateHistorySearchString();
                manager.refresh();
                return State.SEARCHING_FINISHED;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                LOG.info("SearchStateManager.currentEntrySelected(): Current entry got selected, but there is no current entry visible!");
                return this;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // refresh, and let the marker update trigger a match count update
                manager.refresh();
                return this;
            }

            @Override
            public SearchState goDown(final SearchStateManager manager) {
                LOG.info("SearchStateManager.goDown(): Autocomplete is already hidden because of a previous match count update, this doesn't make sense!");
                return this;
            }

            @Override
            public SearchState goUp(final SearchStateManager manager) {
                LOG.info("SearchStateManager.goUp(): Autocomplete is already hidden because of a previous match count update, this doesn't make sense!");
                return this;
            }

            @Override
            public SearchState updateMatchCount(final SearchStateManager manager, final int matchCount) {
                // autocomplete is hidden, so if we have matches now, we should re-show it
                if (matchCount > 0) {
                    manager.showAutocomplete();
                    return SEARCHING_AUTOCOMPLETE_VISIBLE;
                }
                return this;
            }
        },
        // Markers are being filtered, input box has content, autocomplete is hidden.
        SEARCHING_FINISHED {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.focusInput();
                manager.hideAutocomplete();
                manager.refresh();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                manager.clearSearchInput();
                manager.focusInput();
                manager.hideAutocomplete();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.NOT_SEARCHING;
            }

            @Override
            public SearchState goDown(final SearchStateManager manager) {
                // we're "finished" searching, but if the user wishes to start navigation again,
                // they can hit the down-arrow to re-open autocomplete and resume searching
                manager.showAutocomplete();
                manager.focusAutocompleteWidget();
                manager.refresh();
                return SEARCHING_AUTOCOMPLETE_ACTIVE;
            }

            @Override
            public SearchState goUp(final SearchStateManager manager) {
                LOG.warning("Somehow we got an \"up\" event, but the autocomplete listbox should be handling this.  Weird.");
                // we're "finished" searching, so going up won't do anything
                return this;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // user has focused the input box and started typing again
                manager.refresh();
                manager.showAutocomplete();
                return SEARCHING_AUTOCOMPLETE_VISIBLE;
            }

            @Override
            public SearchState finishedSearching(final SearchStateManager manager) {
                // we're already finished searching... finish... again?
                LOG.info("WARNING: attempting to finish search, but we're already finished!");
                return this;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                LOG.info("WARNING: attempting to select an entry, but we're already finished!");
                return this;
            }

            @Override
            public SearchState updateMatchCount(final SearchStateManager manager, final int matchCount) {
                // map count is updated, but search is finished, so autocomplete should stay hidden
                return this;
            }
        };

    }

    private String getHistorySearchString() {
        final String value = m_history.getValue();
        if (value != null && value.startsWith("search/")) {
            return value.replaceFirst("^search\\/", "");
        } else {
            return null;
        }
    }

    protected void updateHistorySearchString() {
        final String token = m_history.getValue();
        final String value = m_valueItem.getValue();
        final String newToken = (value == null || "".equals(value)) ? "" : "search/" + value;
        if (!newToken.equals(token)) {
            m_history.setValue(newToken);
        }
    }

    public abstract void refresh();
    public abstract void entrySelected();
    public abstract void clearSearchInput();
    public abstract void focusInput();
    public abstract void focusAutocompleteWidget();
    public abstract void showAutocomplete();
    public abstract void hideAutocomplete();
}

