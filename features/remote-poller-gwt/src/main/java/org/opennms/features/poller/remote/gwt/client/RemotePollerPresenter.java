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

package org.opennms.features.poller.remote.gwt.client;


import org.opennms.features.poller.remote.gwt.client.FilterPanel.FiltersChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.StatusSelectionChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagClearedEventHandler;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagSelectedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDeselectedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerInfoWindowRefreshHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;



/**
 * This interface represents the controller methods that control the user interface.
 * It extends several event handlers that the controller logic is expected to respond to.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface RemotePollerPresenter extends MapPanelBoundsChangedEventHandler,
		LocationPanelSelectEventHandler,
		FiltersChangedEventHandler,
		TagSelectedEventHandler,
		TagClearedEventHandler,
		StatusSelectionChangedEventHandler,
		GWTMarkerClickedEventHandler,
		GWTMarkerInfoWindowRefreshHandler,
		ApplicationDeselectedEventHandler,
		ApplicationSelectedEventHandler, MapRemoteEventHandler
{
    
    
}
