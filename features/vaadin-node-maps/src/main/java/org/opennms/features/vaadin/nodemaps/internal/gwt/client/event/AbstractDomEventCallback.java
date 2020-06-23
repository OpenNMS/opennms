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

package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractDomEventCallback extends JSObjectWrapper implements DomEventCallback {
    private String[] m_eventTypes;
    private Element m_element;
    private Widget m_widget;

    public AbstractDomEventCallback() {
        super(JSObject.createJSFunction());
        setJSObject(getCallbackFunction(this));
    }

    public AbstractDomEventCallback(final String[] eventTypes) {
        this();
        m_eventTypes = eventTypes;
    }

    public AbstractDomEventCallback(final String eventType) {
        this(new String[]{eventType});
    }

    public AbstractDomEventCallback(final String[] eventTypes, final Widget widget) {
        this(eventTypes);
        m_widget = widget;
    }

    public AbstractDomEventCallback(final String eventType, final Widget widget) {
        this(new String[] { eventType });
        m_widget = widget;
    }

    public AbstractDomEventCallback(final String[] eventTypes, final Element element) {
        this(eventTypes);
        m_element = element;
    }

    public AbstractDomEventCallback(final String eventType, final Element element) {
        this(new String[] { eventType });
        m_element = element;
    }

    public String[] getEventTypes() {
        return m_eventTypes;
    }
    
    public Widget getWidget() {
        return m_widget;
    }

    public void setWidget(final Widget widget) {
        m_widget = widget;
    }

    public Element getElement() {
        if (m_widget == null) {
            if (m_element == null) {
                return Document.get().getDocumentElement();
            } else {
                return m_element;
            }
        } else {
            return m_widget.getElement();
        }
    }

    public void setElement(final Element element) {
        m_element = element;
    }

    protected native JSObject getCallbackFunction(final DomEventCallback callback) /*-{
        var self = callback;
        return function(event) {
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;

}
