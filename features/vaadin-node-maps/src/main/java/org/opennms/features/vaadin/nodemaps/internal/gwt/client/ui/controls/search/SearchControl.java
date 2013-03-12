package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchOptions;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.terminal.gwt.client.VConsole;

public class SearchControl extends Control {
    private HTMLPanel m_container;
    private TextBox m_inputBox;
    private Anchor m_submitAnchor;

    private SearchConsumer m_searchConsumer;
    private SearchEventCallback m_changeCallback;

    private SearchOptions m_options;
    private boolean m_refreshSearch = false;
    private boolean m_timerActive = false;
    private Timer m_timer;

    protected SearchControl(final JSObject element) {
        super(element);
    }

    public SearchControl(final SearchConsumer searchConsumer) {
        this(searchConsumer, new SearchOptions());
    }

    public SearchControl(final SearchConsumer searchConsumer, final SearchOptions options) {
        super(JSObject.createJSObject());
        setJSObject(SearchControlImpl.create(this, options.getJSObject()));
        VConsole.log("new SearchControl()");
        m_searchConsumer = searchConsumer;
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

        return m_container.getElement();
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
        VConsole.log("search field changed to: " + m_inputBox.getValue());
        m_timer.cancel();
        m_timerActive = false;
        m_searchConsumer.setSearchString(m_inputBox.getValue());
        
        if (m_searchConsumer.isSearching()) {
            // update drop-box
        }
        m_searchConsumer.refresh();
    }

    protected SearchControl doOnRemove(final JavaScriptObject map) {
        VConsole.log("onRemove() called");
        if (m_changeCallback != null) DomEvent.removeListener(m_changeCallback);
        return this;
    }
}
