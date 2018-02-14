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
 * <p>LocationPanelSelectEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationPanelSelectEvent extends GwtEvent<LocationPanelSelectEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<LocationPanelSelectEventHandler> TYPE = new Type<>();
    private String m_locationName;
    
    /**
     * <p>Constructor for LocationPanelSelectEvent.</p>
     *
     * @param locationName a {@link java.lang.String} object.
     */
    public LocationPanelSelectEvent(String locationName) {
        m_locationName = locationName;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(LocationPanelSelectEventHandler handler) {
        handler.onLocationSelected(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public Type<LocationPanelSelectEventHandler> getAssociatedType() {
        return TYPE;
    }


    /**
     * <p>getLocationName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocationName() {
        return m_locationName;
    }
}
