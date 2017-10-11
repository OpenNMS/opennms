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

import org.opennms.features.poller.remote.gwt.client.GWTBounds;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>MapPanelBoundsChangedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MapPanelBoundsChangedEvent extends GwtEvent<MapPanelBoundsChangedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<MapPanelBoundsChangedEventHandler> TYPE = new Type<>();
    private GWTBounds m_newBounds;
    
    /**
     * <p>Constructor for MapPanelBoundsChangedEvent.</p>
     *
     * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public MapPanelBoundsChangedEvent(GWTBounds bounds) {
        setBounds(bounds);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(MapPanelBoundsChangedEventHandler handler) {
        handler.onBoundsChanged(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public Type<MapPanelBoundsChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setBounds(GWTBounds bounds) {
        m_newBounds = bounds;
    }

    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds() {
        return m_newBounds;
    }

}
