package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.controls.ControlOptions;

public class SearchOptions extends ControlOptions {
    public SearchOptions() {
        super();
        setPosition("topleft");
        setSearchRefreshInterval(100);
    }

    /** placeholder text in the search box */
    public String getPlaceholder() {
        return getPropertyAsString("placeholder");
    }
    /** placeholder text in the search box */
    public SearchOptions setPlaceholder(final String placeholder) {
        return (SearchOptions)setProperty("placeholder", placeholder);
    }

    /** how often to live-update search results as search-typing is happening **/
    public int getSearchRefreshInterval() {
        return getPropertyAsInt("interval");
    }
    /** how often to live-update search results as search-typing is happening **/
    public SearchOptions setSearchRefreshInterval(final int interval) {
        return (SearchOptions)setProperty("interval", interval);
    }

    public int getPropertyAsInteger(final String name) {
        return getJSObject().getPropertyAsInt(name);
    }

}
