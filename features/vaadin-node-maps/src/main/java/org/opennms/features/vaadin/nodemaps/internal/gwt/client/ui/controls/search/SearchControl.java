package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import java.util.List;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchOptions;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerContainer;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.vaadin.terminal.gwt.client.VConsole;

public class SearchControl extends Control {
    private HTMLPanel m_container;
    private TextBox m_inputBox;
    private HTML m_submitAnchor;

    private SearchConsumer m_searchConsumer;
    private MarkerContainer m_markerContainer;
    private SearchEventCallback m_changeCallback;

    private SearchOptions m_options;
    private boolean m_refreshSearch = false;
    private boolean m_timerActive = false;
    private Timer m_timer;
    private CellList<NodeMarker> m_autoComplete;
    private NodeMarker m_selected;

    protected SearchControl(final JSObject element) {
        super(element);
    }

    public SearchControl(final SearchConsumer searchConsumer, final MarkerContainer markerContainer) {
        this(searchConsumer, markerContainer, new SearchOptions());
    }

    public SearchControl(final SearchConsumer searchConsumer, final MarkerContainer markerContainer, final SearchOptions options) {
        super(JSObject.createJSObject());
        setJSObject(SearchControlImpl.create(this, options.getJSObject()));
        VConsole.log("new SearchControl()");
        m_searchConsumer = searchConsumer;
        m_markerContainer = markerContainer;
        m_options = options;

        m_timer = new Timer() {
            @Override public void run() {
                if (m_refreshSearch) { 
                    updateSearchResults();
                }
            }
            
        };
        
        initializeContainerWidget();
        initializeInputWidget();
        initializeSubmitWidget();
        //initializeAutocompleteWidget();
        initializeCellAutocompleteWidget();
    }

    public void focus() {
        m_inputBox.setFocus(true);
    }

    public Element doOnAdd(final JavaScriptObject map) {
        VConsole.log("onAdd() called");
        
        m_container.add(m_inputBox);
        m_container.add(m_submitAnchor);
        m_container.add(m_autoComplete);

        return m_container.getElement();
    }

    public SearchControl doOnRemove(final JavaScriptObject map) {
        VConsole.log("onRemove() called");
        if (m_changeCallback != null) DomEvent.removeListener(m_changeCallback);
        return this;
    }

    public void refreshAutocomplete() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
                final List<NodeMarker> markers = m_markerContainer.getMarkers();
                if (m_searchConsumer.isSearching()) {
                    m_autoComplete.setVisible(true);
                    updateAutocompleteStyle(m_autoComplete);
                    if (markers.size() == 1) {
                        m_selected = markers.get(0);
                        hideAutocomplete();
                    }
                    m_autoComplete.setRowData(markers);
                } else {
                    hideAutocomplete();
                }
            }
        });
    }

    protected void hideAutocomplete() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override public void execute() {
                m_autoComplete.setVisible(false);
                if (m_selected != null) {
                    m_autoComplete.getSelectionModel().setSelected(m_selected, false);
                    m_selected = null;
                }
            }
        });
    }

    protected void updateAutocompleteStyle(final Widget widget) {
        final Style style = widget.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        final int left = 5;
        final int top = m_container.getOffsetHeight() + 5;
        //VConsole.log("left = " + left + ", top = " + top);
        style.setLeft(left, Unit.PX);
        style.setTop(top, Unit.PX);
        DomEvent.stopEventPropagation(widget);
    }

    protected void handleSearchEvent(final NativeEvent event) {
        final Element target = event.getEventTarget().cast();
        if (target.equals(m_submitAnchor.getElement())) {
            VConsole.log("event received on the anchor, preventing default");
            event.preventDefault();
        }
        if (event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
            m_inputBox.setText("");
        } else if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
            final List<NodeMarker> markers = m_markerContainer.getMarkers();
            if (markers.size() > 0) {
                m_autoComplete.getSelectionModel().setSelected(markers.get(0), true);
            }
            m_autoComplete.setFocus(true);
            m_autoComplete.setVisible(true);
        }
        m_refreshSearch = true;
        if (!m_timerActive) {
            m_timerActive = true;
            m_timer.scheduleRepeating(m_options.getSearchRefreshInterval());
        }
    }

    protected void updateSearchResults() {
        m_timer.cancel();
        m_timerActive = false;
        m_searchConsumer.setSearchString(m_inputBox.getValue());
        m_searchConsumer.refresh();
        this.refreshAutocomplete();
    }

    private void initializeContainerWidget() {
        m_container = HTMLPanel.wrap(SearchControlImpl.createElement("leaflet-control-search"));
        m_container.addStyleName("leaflet-control");
    }

    private void initializeInputWidget() {
        m_inputBox = new TextBox();
        m_inputBox.addStyleName("search-input");
        m_inputBox.getElement().setAttribute("placeholder", "Search...");
        m_inputBox.getElement().setAttribute("type", "search");
        m_inputBox.setMaxLength(40);
        m_inputBox.setVisibleLength(40);

        DomEvent.stopEventPropagation(m_inputBox);

        m_changeCallback = new SearchEventCallback(new String[] { "keydown", "change", "cut", "paste", "search" }, m_inputBox, m_searchConsumer) {
            @Override protected void onEvent(final NativeEvent event) {
                handleSearchEvent(event);
            }
        };
        DomEvent.addListener(m_changeCallback);
    }

    private void initializeSubmitWidget() {
        m_submitAnchor = new HTML();
        m_submitAnchor.addStyleName("search-button");
        m_submitAnchor.setTitle("Search locations...");

        DomEvent.stopEventPropagation(m_submitAnchor);
        DomEvent.addListener(new DomEventCallback("click", m_submitAnchor) {
            @Override
            protected void onEvent(final NativeEvent event) {
                m_inputBox.setFocus(true);
                handleSearchEvent(event);
            }
        });
    }

    private void initializeCellAutocompleteWidget() {
        final AbstractSafeHtmlRenderer<NodeMarker> renderer = new AbstractSafeHtmlRenderer<NodeMarker>() {
            @Override
            public SafeHtml render(final NodeMarker marker) {
                final SafeHtmlBuilder builder = new SafeHtmlBuilder();
                final String searchString = m_searchConsumer.getSearchString().toLowerCase();

                builder.appendHtmlConstant("<div class=\"autocomplete-label\">");
                builder.appendHtmlConstant(marker.getNodeLabel());
                builder.appendHtmlConstant("</div>");
                String additionalSearchInfo = null;
                if (searchString.startsWith("category:")) {
                    final String categoryString = marker.getCategoriesAsString();
                    if (categoryString.length() > 0) {
                        additionalSearchInfo = categoryString;
                    }
                } else if (searchString.startsWith("nodelabel:")) {
                    // do nothing, we already show this
                } else if (searchString.startsWith("description")) {
                    additionalSearchInfo = "Description: " + marker.getDescription();
                } else if (searchString.startsWith("ipaddress")) {
                    additionalSearchInfo = "IP Address: " + marker.getIpAddress();
                } else if (searchString.startsWith("maintcontract")) {
                    additionalSearchInfo = "Maint.&nbsp;Contract: " + marker.getMaintContract();
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
                boolean setSearchString = false;
                final String eventType = event.getType();
                if ("click".equals(eventType) || "touchstart".equals(eventType)) {
                    if (m_autoComplete.getSelectionModel().isSelected(value)) {
                        setSearchString = true;
                    }
                } else if ("dblclick".equals(eventType)) {
                    setSearchString = true;
                } else if ("keydown".equals(eventType)) {
                    if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                        setSearchString = true;
                    } else if (event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        m_inputBox.setText("");
                        setSearchString = false;
                        hideAutocomplete();
                    }
                }

                if (setSearchString) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override public void execute() {
                            m_autoComplete.getSelectionModel().setSelected(value, false);
                            hideAutocomplete();
                            m_inputBox.setFocus(true);
                            m_inputBox.setValue(value.getNodeLabel(), true);
                            m_searchConsumer.setSearchString(value.getNodeLabel());
                            m_searchConsumer.refresh();
                        }
                    });
                } else {
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
        m_autoComplete.setSelectionModel(new SingleSelectionModel<NodeMarker>());
        m_autoComplete.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        m_autoComplete.setVisible(false);
        m_autoComplete.addStyleName("search-autocomplete");
    }
}
