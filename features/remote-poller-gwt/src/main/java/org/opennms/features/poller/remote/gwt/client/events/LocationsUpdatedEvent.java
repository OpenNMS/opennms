/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>LocationsUpdatedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationsUpdatedEvent extends GwtEvent<LocationsUpdatedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<LocationsUpdatedEventHandler> TYPE = new Type<>();

    private String m_eventString = "You have got the event String";
    
    /**
     * <p>Constructor for LocationsUpdatedEvent.</p>
     */
    public LocationsUpdatedEvent() {
    }
    
	/** {@inheritDoc} */
	@Override
	protected void dispatch(LocationsUpdatedEventHandler handler) {
		handler.onLocationsUpdated(this);
	}

	/** {@inheritDoc} */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<LocationsUpdatedEventHandler> getAssociatedType() {
		return TYPE;
	}

    /**
     * <p>setEventString</p>
     *
     * @param eventString a {@link java.lang.String} object.
     */
    public void setEventString(final String eventString) {
        m_eventString = eventString;
    }

    /**
     * <p>getEventString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEventString() {
        return m_eventString;
    }
}
