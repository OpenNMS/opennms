package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;

public class SearchControlImpl {

    public static native final JSObject create(final JSObject options) /*-{
        var self = this;
        $wnd.L.Control.MarkerSearch = $wnd.L.Control.extend({
            includes: $wnd.L.Mixin.Events,
            options: {
                position: 'topleft'
            }
//            initialize: function(options) {
//                var searchOptions = @org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchOptions::new()();
//                searchOptions.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchOptions::setJSObject(Lorg/discotools/gwt/leaflet/client/jsobject/JSObject;)(options);
//                self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchControl::initialize(Lorg/opennms/features/vaadin/nodemaps/internal/gwt/client/SearchOptions;)(searchOptions);
//            }
        });
        
        return new $wnd.L.Control.MarkerSearch(options);
    }-*/;

}
