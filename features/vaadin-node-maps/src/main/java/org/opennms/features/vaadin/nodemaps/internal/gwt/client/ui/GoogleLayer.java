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

    @Override
    public GoogleLayer setOptions(final Options options) {
        return (GoogleLayer)super.setOptions(options);
    }

}
