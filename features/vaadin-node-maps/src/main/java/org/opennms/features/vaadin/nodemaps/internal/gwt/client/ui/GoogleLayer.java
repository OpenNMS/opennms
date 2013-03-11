package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.ILayer;

public class GoogleLayer extends ILayer {

        protected GoogleLayer(final JSObject element) {
                super(element);
        }

        public GoogleLayer(final String type) {
            this(getImpl(type, null));
        }

        public GoogleLayer(final String type, final Options options) {
            this(getImpl(type, options));
        }

        protected static final JSObject getImpl(final String type, final Options options) {
            return GoogleLayerImpl.create(type, options == null? JSObject.createJSObject() : options.getJSObject());
        }

        public GoogleLayer setOptions(final Options options) {
                return (GoogleLayer)super.setOptions(options);
        }
                
}
