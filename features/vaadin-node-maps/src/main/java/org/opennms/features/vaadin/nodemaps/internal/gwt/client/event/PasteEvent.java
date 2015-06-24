/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import com.google.gwt.event.dom.client.DomEvent;

/**
 * Represents a native change event.
 */
public class PasteEvent extends DomEvent<PasteHandler> {

    /**
     * Event type for Paste events. Represents the meta-data associated with this
     * event.
     */
    private static final Type<PasteHandler> TYPE = new Type<PasteHandler>("paste", new PasteEvent());

    /**
     * Gets the event type associated with change events.
     * 
     * @return the handler type
     */
    public static Type<PasteHandler> getType() {
        return TYPE;
    }

    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire change events.
     */
    protected PasteEvent() {
    }

    @Override
    public final Type<PasteHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final PasteHandler handler) {
        handler.onPaste(this);
    }

}
