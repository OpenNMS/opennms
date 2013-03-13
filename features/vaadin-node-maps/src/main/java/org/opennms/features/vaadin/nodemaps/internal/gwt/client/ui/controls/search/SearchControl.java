package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import java.util.ListIterator;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchOptions;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerContainer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.terminal.gwt.client.VConsole;

public class SearchControl extends Control {
    private HTMLPanel m_container;
    private TextBox m_inputBox;
    private Anchor m_submitAnchor;

    private SearchConsumer m_searchConsumer;
    private MarkerContainer m_markerContainer;
    private SearchEventCallback m_changeCallback;

    private SearchOptions m_options;
    private boolean m_refreshSearch = false;
    private boolean m_timerActive = false;
    private Timer m_timer;
    private HTML m_autocomplete;

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
    }

    public void focus() {
        m_inputBox.setFocus(true);
    }

    public Element doOnAdd(final JavaScriptObject map) {
        VConsole.log("onAdd() called");
        
        m_container = HTMLPanel.wrap(SearchControlImpl.createElement("leaflet-control-search"));
        m_container.addStyleName("leaflet-control");

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

        m_container.add(m_inputBox);

        m_submitAnchor = new Anchor();
        m_submitAnchor.addStyleName("search-button");
        m_submitAnchor.setTitle("Search locations...");
        m_submitAnchor.setHref("#");
        m_submitAnchor.setTabIndex(-1);

        DomEvent.stopEventPropagation(m_submitAnchor);
        DomEvent.addListener(new DomEventCallback("click", m_submitAnchor) {
            @Override
            protected void onEvent(final NativeEvent event) {
                handleSearchEvent(event);
            }
        });

        m_container.add(m_submitAnchor);

        m_autocomplete = new HTML("No matches.");
        m_autocomplete.setVisible(false);
        m_autocomplete.addStyleName("search-autocomplete");
        updateAutocompleteStyle();

        m_container.add(m_autocomplete);

        return m_container.getElement();
    }

    private void updateAutocompleteStyle() {
        final Style style = m_autocomplete.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        final int left = 5;
        final int top = m_container.getOffsetHeight() + 5;
        //VConsole.log("left = " + left + ", top = " + top);
        style.setLeft(left, Unit.PX);
        style.setTop(top, Unit.PX);
        DomEvent.stopEventPropagation(m_autocomplete);
    }

    private void handleSearchEvent(final NativeEvent event) {

        final Element target = event.getEventTarget().cast();
        if (target.equals(m_submitAnchor.getElement())) {
            VConsole.log("event received on the anchor, preventing default");
            event.preventDefault();
        }
        if (event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
            m_inputBox.setText("");
        }
        m_refreshSearch = true;
        if (!m_timerActive) {
            m_timerActive = true;
            m_timer.scheduleRepeating(m_options.getSearchRefreshInterval());
        }
    }

    private void updateSearchResults() {
        //VConsole.log("search field changed to: " + m_inputBox.getValue());
        m_timer.cancel();
        m_timerActive = false;
        m_searchConsumer.setSearchString(m_inputBox.getValue());
        m_searchConsumer.refresh();
        this.refreshAutocomplete();
    }

    public void refreshAutocomplete() {
        if (m_searchConsumer.isSearching()) {
            final String searchString = m_searchConsumer.getSearchString().toLowerCase();

            Scheduler.get().scheduleIncremental(new RepeatingCommand() {
                final ListIterator<NodeMarker> m_markerIterator = m_markerContainer.listIterator();
                final StringBuilder m_sb = new StringBuilder();

                @Override public boolean execute() {
                    if (m_markerIterator.hasNext()) {
                        final NodeMarker marker = m_markerIterator.next();
                        m_sb.append("<li>");
                        m_sb.append("<div class=\"autocomplete-entry\">");
                        m_sb.append(marker.getNodeLabel());
                        m_sb.append("</div>");
                        String additionalSearchInfo = null;
                        if (searchString.startsWith("category:")) {
                            final StringBuilder catBuilder = new StringBuilder();
                            final JsArrayString categories = marker.getCategories();
                            if (categories.length() > 0) {
                                if (categories.length() == 1) {
                                    catBuilder.append("Category: ");
                                } else {
                                    catBuilder.append("Categories: ");
                                }
                                for (int i = 0; i < categories.length(); i++) {
                                    catBuilder.append(categories.get(i));
                                    if (i != (categories.length() - 1)) {
                                        catBuilder.append(", ");
                                    }
                                }
                                additionalSearchInfo = catBuilder.toString();
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
                            m_sb.append("<div class=\"autocomplete-additional-info\">").append(additionalSearchInfo).append("</div>");
                        }
                        m_sb.append("</li>");
                        return true;
                    }

                    m_autocomplete.setHTML("<ul>" + m_sb.toString() + "</ul>");
                    m_autocomplete.setVisible(true);
                    updateAutocompleteStyle();
                    
                    return false;
                }
            });
        } else {
            m_autocomplete.setVisible(false);
            m_autocomplete.setHTML("No matches.");
        }
    }

    protected SearchControl doOnRemove(final JavaScriptObject map) {
        VConsole.log("onRemove() called");
        if (m_changeCallback != null) DomEvent.removeListener(m_changeCallback);
        return this;
    }
}
