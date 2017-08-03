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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ComponentTracker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.OpenNMSEventManager;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.CutEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilteredMarkersUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilteredMarkersUpdatedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.PasteEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchStringSetEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchStringSetEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerContainer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.shared.Util;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class SearchControl extends AbsolutePanel implements FilteredMarkersUpdatedEventHandler, SearchStringSetEventHandler {
    private Logger LOG = Logger.getLogger(getClass().getName());

    private static final Map<String, String> m_labels;
    static {
        m_labels = new HashMap<String,String>();
        m_labels.put("nodeid", "Node&nbsp;ID");
        m_labels.put("description", "Description");
        m_labels.put("ipaddress", "IP&nbsp;Address");
        m_labels.put("maintcontract", "Maint.&nbsp;Contract");
        m_labels.put("foreignsource", "Foreign&nbsp;Source");
        m_labels.put("foreignid", "Foreign&nbsp;ID");
    }

    private SearchTextBox m_inputBox;
    private HistoryWrapper m_historyWrapper;

    private MarkerContainer m_markerContainer;

    private CellList<NodeMarker> m_autoComplete;
    private SearchStateManager m_stateManager;
    private SingleSelectionModel<NodeMarker> m_selectionModel;
    private Set<Widget> m_updated = new HashSet<>();

    private final OpenNMSEventManager m_eventManager;
    private final ComponentTracker m_componentTracker;

    public SearchControl(final MarkerContainer markerContainer, final Widget root, final OpenNMSEventManager eventManager, final ComponentTracker componentTracker) {
        super();
        LOG.info("new SearchControl()");

        m_eventManager = eventManager;
        m_componentTracker = componentTracker;

        m_markerContainer = markerContainer;
        m_selectionModel = new SingleSelectionModel<>();
        m_historyWrapper = new HistoryWrapper();

        initializeContainerWidget();
        initializeInputWidget();
        initializeCellAutocompleteWidget();
        initializeSearchStateManager();

        addAttachHandler(new Handler() {
            @Override public void onAttachOrDetach(final AttachEvent event) {
                if (event.isAttached()) {
                    doOnAdd();
                } else {
                    doOnRemove();
                }
            }
        });
    }

    public Element doOnAdd() {
        LOG.info("SearchControl.onAdd() called");

        this.add(m_inputBox);
        this.add(m_autoComplete);

        /* If the backend sends a new search string, set it on the input box
         * to make sure we're in sync, but don't re-fire events.
         */
        m_eventManager.addHandler(SearchStringSetEvent.TYPE, this);
        m_eventManager.addHandler(FilteredMarkersUpdatedEvent.TYPE, this);

        final SearchEventHandler searchEventHandler = new SearchEventHandler() {
            @Override protected void onEvent(final DomEvent<? extends EventHandler> event) {
                m_stateManager.handleInputEvent(event.getNativeEvent());
            }

        };

        m_autoComplete.addHandler(new KeyDownHandler() {
            @Override public void onKeyDown(final KeyDownEvent event) {
                m_stateManager.handleAutocompleteEvent(event.getNativeEvent());
            }
        }, KeyDownEvent.getType());

        m_inputBox.addKeyDownHandler(searchEventHandler);
        m_inputBox.addChangeHandler(searchEventHandler);
        m_inputBox.addClickHandler(new ClickHandler() {
            @Override public void onClick(final ClickEvent event) {
                SearchEvent.fireNativeEvent(Document.get().createChangeEvent(), m_inputBox);
            }
        });
        m_inputBox.addHandler(searchEventHandler, CutEvent.getType());
        m_inputBox.addHandler(searchEventHandler, PasteEvent.getType());
        m_inputBox.addHandler(searchEventHandler, SearchEvent.getType());

        m_componentTracker.ready(getClass());

        return this.getElement();
    }

    public SearchControl doOnRemove() {
        LOG.info("SearchControl.onRemove() called");
        m_eventManager.removeHandler(SearchStringSetEvent.TYPE, this);
        m_eventManager.removeHandler(FilteredMarkersUpdatedEvent.TYPE, this);
        return this;
    }

    public void refresh() {
        m_autoComplete.setRowData(m_markerContainer.getMarkers());
    }

    protected void updateAutocompleteStyle(final Widget widget) {
        // we only need to do this once
        if (m_updated.contains(widget)) {
            return;
        }

        final Style style = widget.getElement().getStyle();
        // ugh
        style.setPosition(Position.ABSOLUTE);
        style.setLeft(5, Unit.PX);
        style.setTop(25, Unit.PX);
        m_updated.add(widget);
    }

    private void initializeSearchStateManager() {
        m_stateManager = new SearchStateManager(m_inputBox, m_historyWrapper, m_eventManager, m_componentTracker) {
            @Override
            public void refresh() {
                LOG.info("SearchControl.SearchStateManager.refresh()");
                sendSearchStringSetEvent(m_inputBox.getValue());

                final List<JSNodeMarker> markers = m_markerContainer.getMarkers();
                final NodeMarker selected = m_selectionModel.getSelectedObject();
                if (selected != null) {
                    if (markers.contains(selected)) {
                        m_selectionModel.setSelected(selected, true);
                    }
                }

                updateMatchCount(markers.size());
                m_eventManager.fireEvent(new FilteredMarkersUpdatedEvent());
            }

            @Override
            public void clearSearchInput() {
                LOG.info("SearchControl.SearchStateManager.clearSearchInput()");
                m_inputBox.setValue("");
                sendSearchStringSetEvent("");
            }

            @Override
            public void focusAutocompleteWidget() {
                LOG.info("SearchControl.SearchStateManager.focusAutocomplete()");
                final NodeMarker selected = m_selectionModel.getSelectedObject();
                if (selected == null) {
                    final List<JSNodeMarker> markers = m_markerContainer.getMarkers();
                    if (markers.size() > 0) {
                        m_selectionModel.setSelected(markers.get(0), true);
                    }
                }
                m_autoComplete.setFocus(true);
            }

            @Override
            public void showAutocomplete() {
                LOG.info("SearchControl.SearchStateManager.showAutocomplete()");
                /*
                final List<JSNodeMarker> markers = m_markerContainer.getMarkers();
                if (markers.size() > 0) {
                    m_selectionModel.setSelected(markers.get(0), true);
                }
                 */
                m_autoComplete.setVisible(true);
                updateAutocompleteStyle(m_autoComplete);
                // m_autoComplete.setFocus(true);
            }

            @Override
            public void hideAutocomplete() {
                LOG.info("SearchControl.SearchStateManager.hideAutocomplete()");
                m_autoComplete.setVisible(false);
            }

            @Override
            public void entrySelected() {
                LOG.info("SearchControl.SearchStateManager.entrySelected()");
                final NodeMarker selected = m_selectionModel.getSelectedObject();
                if (selected != null) {
                    final String newSearchString = "nodeLabel=" + selected.getNodeLabel();
                    m_inputBox.setValue(newSearchString);
                    m_selectionModel.setSelected(selected, false);
                    m_inputBox.setFocus(true);
                    sendSearchStringSetEvent(newSearchString);
                } else {
                    LOG.warning("entrySelected() but nothing in the selection model.");
                }
            }

            @Override
            public void focusInput() {
                LOG.info("SearchControl.SearchStateManager.focusInput()");
                m_inputBox.setFocus(true);
            }
        };
    }

    private void setIdIfMissing(final Widget widget, final String id) {
        if (widget.getElement() != null) {
            final String existingId = widget.getElement().getId();
            if (existingId == null || "".equals(existingId)) {
                widget.getElement().setId(id);
            }
        }
    }

    private void initializeContainerWidget() {
        this.setStylePrimaryName("leaflet-control-search");
        // this.addStyleName("leaflet-bar");
        this.addStyleName("leaflet-control");
        this.getElement().getStyle().setOverflow(Overflow.VISIBLE);
        // this.setWidth("100%");
        // this.setHeight("100%");
    }

    private void initializeInputWidget() {
        m_inputBox = new SearchTextBox();
        m_inputBox.addStyleName("search-input");
        m_inputBox.getElement().setAttribute("placeholder", "Search...");
        m_inputBox.getElement().setAttribute("type", "search");
        m_inputBox.setMaxLength(40);
        m_inputBox.setVisibleLength(40);
        m_inputBox.setValue("");
        setIdIfMissing(m_inputBox, "searchControl.searchInput");
    }

    private void initializeCellAutocompleteWidget() {
        final AbstractSafeHtmlRenderer<NodeMarker> renderer = new AbstractSafeHtmlRenderer<NodeMarker>() {
            @Override
            public SafeHtml render(final NodeMarker marker) {
                final SafeHtmlBuilder builder = new SafeHtmlBuilder();
                final String search = m_inputBox.getValue();

                builder.appendHtmlConstant("<div class=\"autocomplete-label\">");
                builder.appendHtmlConstant(marker.getNodeLabel());
                builder.appendHtmlConstant("</div>");
                String additionalSearchInfo = null;

                if (search != null && (search.contains(":") || search.contains("="))) {
                    final String searchKey = search.replaceAll("[\\:\\=].*$", "").toLowerCase();
                    LOG.info("searchKey = " + searchKey);

                    final Map<String,String> props = marker.getProperties();

                    if ("category".equals(searchKey) || "categories".equals(searchKey)) {
                        final String catString = props.get("categories");
                        if (catString != null) {
                            additionalSearchInfo = catString;
                        }
                    }

                    for (final Map.Entry<String,String> entry : props.entrySet()) {
                        final String key = entry.getKey().toLowerCase();
                        final Object value = entry.getValue();
                        if (key.equals(searchKey) && m_labels.containsKey(key)) {
                            additionalSearchInfo = m_labels.get(key) + ": " + value;
                            break;
                        }
                    }
                }

                if (additionalSearchInfo != null) {
                    builder.appendHtmlConstant("<div class=\"autocomplete-additional-info\">")
                    .appendHtmlConstant(additionalSearchInfo)
                    .appendHtmlConstant("</div>");
                }

                return builder.toSafeHtml();
            }
        };

        final AbstractSafeHtmlCell<NodeMarker> cell = new AbstractSafeHtmlCell<NodeMarker>(renderer, "keydown", "click", "dblclick", "touchstart") {

            @Override
            public void onBrowserEvent(final Context context, final com.google.gwt.dom.client.Element parent, final NodeMarker value, final NativeEvent event, final ValueUpdater<NodeMarker> valueUpdater) {
                LOG.info("SearchControl.AutocompleteCell.onBrowserEvent(): context = " + context + ", parent = " + parent + ", value = " + value + ", event = " + event);
                if (m_stateManager.handleAutocompleteEvent(event)) {
                    super.onBrowserEvent(context, parent, value, event, valueUpdater);
                }
            }

            @Override protected void render(final Context context, final SafeHtml data, final SafeHtmlBuilder builder) {
                builder.appendHtmlConstant("<div class=\"autocomplete-entry\">");
                if (data != null) {
                    builder.append(data);
                }
                builder.appendHtmlConstant("</div>");
            }
        };

        m_autoComplete = new CellList<NodeMarker>(cell);
        m_autoComplete.setSelectionModel(m_selectionModel);
        m_autoComplete.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        m_autoComplete.setVisible(false);
        m_autoComplete.addStyleName("search-autocomplete");
        setIdIfMissing(m_autoComplete, "searchControl.autoComplete");
    }

    private static class HistoryWrapper implements ValueItem {
        @Override
        public String getValue() {
            return History.getToken();
        }

        @Override
        public void setValue(final String value) {
            History.newItem(value);
        }


    }

    public void replaceSearchWith(final String newSearchString) {
        if (m_inputBox != null) {
            final String existingSearchString = m_inputBox.getValue();
            if (Util.hasChanged(existingSearchString, newSearchString)) {
                LOG.info("SearchControl.replaceSearchWith(" + newSearchString + "): updated.");
                m_inputBox.setValue(newSearchString, false);
                return;
            }
        }
        LOG.info("SearchControl.replaceSearchWith(" + newSearchString + "): unmodified.");
    }

    public String getSearchString() {
        return m_inputBox.getValue();
    }

    @Override public void onFilteredMarkersUpdatedEvent(final FilteredMarkersUpdatedEvent event) {
        LOG.info("SearchControl.onFilteredMarkersUpdated()");
        refresh();
    }

    @Override
    public void onSearchStringSet(final SearchStringSetEvent event) {
        replaceSearchWith(event.getSearchString());
    }

    public void focusInput() {
        m_inputBox.setFocus(true);
    }
}
