package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.vaadin.terminal.gwt.client.VConsole;

public abstract class SearchStateManager {
    private SearchState m_state;
    private ValueItem m_valueItem;
    private ValueItem m_history;

    public SearchStateManager(final ValueItem valueItem, final ValueItem history) {
        m_valueItem = valueItem;
        m_history = history;
        
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
        VConsole.log("handleAutocompleteEvent(" + m_state + "): received " + eventType + " (keyCode = " + eventKeyCode + ")");

        if ("keydown".equals(eventType)) {
            switch (eventKeyCode) {
            case KeyCodes.KEY_ESCAPE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.cancelSearching(SearchStateManager.this);
                    }
                });
                break;
            case KeyCodes.KEY_ENTER:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.currentEntrySelected(SearchStateManager.this);
                    }
                });
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
            return true;
        } else {
            VConsole.log("handleAutocompleteEvent(" + m_state + "): unhandled event: " + eventType);
            return true;
        }
        return false;
    }

    public void handleSearchIconEvent(final NativeEvent event) {
        final String eventType = event.getType();
        VConsole.log("handleSearchIconEvent(" + m_state + "): received " + eventType + " (keyCode = " + event.getKeyCode() + ")");

        if ("click".equals(eventType) || "touchstart".equals(eventType)) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    m_state = m_state.finishedSearching(SearchStateManager.this);
                }
            });
        } else {
            VConsole.log("handleSearchIconEvent(" + m_state + "): unhandled event: " + eventType);
        }
    }

    public void handleInputEvent(final NativeEvent event) {
        final String eventType = event.getType();
        VConsole.log("handleInputEvent(" + m_state + "): received " + eventType + " (keyCode = " + event.getKeyCode() + ")");

        if ("keydown".equals(eventType)) {
            switch (event.getKeyCode()) {
            case KeyCodes.KEY_ESCAPE:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.cancelSearching(SearchStateManager.this);
                    }
                });
                break;
            case KeyCodes.KEY_DOWN:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.autocompleteStartNavigation(SearchStateManager.this);
                    }
                });
                break;
            case KeyCodes.KEY_ENTER:
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.finishedSearching(SearchStateManager.this);
                    }
                });
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
                        }
                    }
                });
                break;
            }
        } else if ("search".equals(eventType)) {
            final String searchString = m_valueItem.getValue();
            if ("".equals(searchString)) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        m_state = m_state.cancelSearching(SearchStateManager.this);
                    }
                });
            }
        } else {
            VConsole.log("handleInputEvent(" + m_state + "): unhandled event: " + eventType);
        }
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
        public abstract SearchState autocompleteStartNavigation(SearchStateManager manager);
        public abstract SearchState updateMatchCount(SearchStateManager manager, int matchCount);
    }

    enum State implements SearchState {
        // Markers are not being filtered, input box is empty, autocomplete is hidden.
        NOT_SEARCHING {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.focusInput();
                manager.hideAutocomplete();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                // make sure input is focused
                manager.focusInput();
                return this;
            }

            @Override
            public SearchState autocompleteStartNavigation(final SearchStateManager manager) {
                // if we're not searching, starting navigation won't do
                // anything because
                // we don't have a search phrase yet
                VConsole.log("WARNING: attempting to start autocomplete navigation, but we're not searching!");
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
                VConsole.log("WARNING: attempting to finish, but we're not searching!");
                return this;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                // if we're not searching, we can't select an entry
                VConsole.log("WARNING: attempting to finish, but we're not searching!");
                return this;
            }

            @Override
            public SearchState updateMatchCount(final SearchStateManager manager, final int matchCount) {
                VConsole.log("WARNING: match count updated, but we're not searching!");
                return this;
            }
        },
        // Markers are being filtered, input box has content, autocomplete is visible.
        SEARCHING_AUTOCOMPLETE_VISIBLE {
            @Override
            public SearchState initialize(final SearchStateManager manager) {
                manager.focusInput();
                manager.showAutocomplete();
                manager.refresh();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                manager.clearSearchInput();
                manager.hideAutocomplete();
                manager.updateHistorySearchString();
                manager.refresh();
                return State.NOT_SEARCHING;
            }

            @Override
            public SearchState autocompleteStartNavigation(final SearchStateManager manager) {
                // we are already searching with autocomplete visible, but
                // input came
                // to the input box, so we return focus to the autocomplete
                // box
                manager.focusAutocomplete();
                return SEARCHING_AUTOCOMPLETE_ACTIVE;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // we're still searching, so show/update autocomplete, and
                // then update markers
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
                manager.hideAutocomplete();
                manager.entrySelected();
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
                manager.focusAutocomplete();
                manager.refresh();
                return this;
            }

            @Override
            public SearchState cancelSearching(final SearchStateManager manager) {
                manager.clearSearchInput();
                manager.hideAutocomplete();
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
            public SearchState autocompleteStartNavigation(final SearchStateManager manager) {
                // navigation has already started
                VConsole.log("WARNING: attempting to start navigation when it has already started");
                return this;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                // the user has clicked an entry or hit enter
                manager.hideAutocomplete();
                manager.entrySelected();
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
                manager.hideAutocomplete();
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
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                VConsole.log("Current entry got selected, but there is no current entry visible!");
                return this;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // refresh, and let the marker update trigger a match count update
                manager.refresh();
                return this;
            }

            @Override
            public SearchState autocompleteStartNavigation(final SearchStateManager manager) {
                VConsole.log("Autocomplete is already hidden because of a previous match count update, this doesn't make sense!");
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
            public SearchState autocompleteStartNavigation(final SearchStateManager manager) {
                // we're "finished" searching, but if the user wishes to start navigation again,
                // they can hit the down-arrow to re-open autocomplete and resume searching
                manager.showAutocomplete();
                manager.focusAutocomplete();
                manager.refresh();
                return SEARCHING_AUTOCOMPLETE_ACTIVE;
            }

            @Override
            public SearchState searchInputReceived(final SearchStateManager manager) {
                // user has focused the input box and started typing again
                manager.refresh();
                manager.showAutocomplete();
                manager.focusInput();
                return SEARCHING_AUTOCOMPLETE_VISIBLE;
            }

            @Override
            public SearchState finishedSearching(final SearchStateManager manager) {
                // we're already finished searching... finish... again?
                VConsole.log("WARNING: attempting to finish search, but we're already finished!");
                return this;
            }

            @Override
            public SearchState currentEntrySelected(final SearchStateManager manager) {
                VConsole.log("WARNING: attempting to select an entry, but we're already finished!");
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
    public abstract void focusAutocomplete();
    public abstract void showAutocomplete();
    public abstract void hideAutocomplete();

}