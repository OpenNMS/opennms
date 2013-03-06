package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.Options;

public class SearchOptions extends Options {
    private NodeMarkerSearchCallback m_callback;

    /** Minimal text length for autocomplete. */
    public int getMinLength() {
        return getPropertyAsInt("minLength");
    }
    /** Minimal text length for autocomplete. */
    public SearchOptions setMinLength(final int minLength) {
        return (SearchOptions)setProperty("minLength", minLength);
    }

    public boolean getInitial() {
        return getPropertyAsBoolean("initial");
    }
    public SearchOptions setInitial(final boolean initial) {
        return (SearchOptions)setProperty("initial", initial);
    }

    /** limit max results to show in tooltip. -1 for no limit. */
    public int getToolTipLimit() {
        return getPropertyAsInt("toolTipLimit");
    }
    /** limit max results to show in tooltip. -1 for no limit. */
    public SearchOptions setToolTipLimit(final int toolTipLimit) {
        return (SearchOptions)setProperty("toolTipLimit", toolTipLimit);
    }
    
    /** auto map panTo when click on tooltip */
    public boolean getTipAutoSubmit() {
        return getPropertyAsBoolean("tipAutoSubmit");
    }
    /** auto map panTo when click on tooltip */
    public SearchOptions setTipAutoSubmit(final boolean tipAutoSubmit) {
        return (SearchOptions)setProperty("tipAutoSubmit", tipAutoSubmit);
    }
    
    /** autoresize on input change */
    public boolean getAutoResize() {
        return getPropertyAsBoolean("autoResize");
    }
    /** autoresize on input change */
    public SearchOptions setAutoResize(final boolean autoResize) {
        return (SearchOptions)setProperty("autoResize", autoResize);
    }
    
    /** collapse search control after submit */
    public boolean getAutoCollapse() {
        return getPropertyAsBoolean("autoCollapse");
    }
    /** collapse search control after submit */
    public SearchOptions setAutoCollapse(final boolean autoCollapse) {
        return (SearchOptions)setProperty("autoCollapse", autoCollapse);
    }
    
    /** delay for autoclosing alert and collapse after blur */
    public int getAutoCollapseTime() {
        return getPropertyAsInteger("autoCollapseTime");
    }
    /** delay for autoclosing alert and collapse after blur */
    public SearchOptions setAutoCollapseTime(final boolean autoCollapseTime) {
        return (SearchOptions)setProperty("autoCollapseTime", autoCollapseTime);
    }
    
    /** animate a circle over location found */
    public boolean getAnimateLocation() {
        return getPropertyAsBoolean("animateLocation");
    }
    /** animate a circle over location found */
    public SearchOptions setAnimateLocation(final boolean animateLocation) {
        return (SearchOptions)setProperty("animateLocation", animateLocation);
    }
    
    /** draw a marker in location found */
    public boolean getMarkerLocation() {
        return getPropertyAsBoolean("markerLocation");
    }
    /** draw a marker in location found */
    public SearchOptions setMarkerLocation(final boolean markerLocation) {
        return (SearchOptions)setProperty("markerLocation", markerLocation);
    }

    /** placeholder value */
    public String getText() {
        return getPropertyAsString("text");
    }
    /** placeholder value */
    public SearchOptions setText(final String text) {
        return (SearchOptions)setProperty("text", text);
    }

    /** name of cancel button */
    public String getTextCancel() {
        return getPropertyAsString("textCancel");
    }
    /** name of cancel button */
    public SearchOptions setTextCancel(final String textCancel) {
        return (SearchOptions)setProperty("textCancel", textCancel);
    }

    /** error message */
    public String getTextErr() {
        return getPropertyAsString("textErr");
    }
    /** error message */
    public SearchOptions setTextErr(final String textErr) {
        return (SearchOptions)setProperty("textErr", textErr);
    }

    /** position */
    public String getPosition() {
        return getPropertyAsString("position");
    }
    /** position */
    public SearchOptions setPosition(final String position) {
        return (SearchOptions)setProperty("position", position);
    }

    /** search callback */
    public NodeMarkerSearchCallback getSearchCallback() {
        return m_callback;
    }
    /** search callback */
    public SearchOptions setSearchCallback(final NodeMarkerSearchCallback callback) {
        m_callback = callback;
        return (SearchOptions)setProperty("searchCall", callback);
    }

    public int getPropertyAsInteger(final String name) {
        return getJSObject().getPropertyAsInt(name);
    }
}
