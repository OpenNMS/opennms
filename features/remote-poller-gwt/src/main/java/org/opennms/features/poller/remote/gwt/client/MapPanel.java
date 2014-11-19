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

import com.google.gwt.user.client.ui.Widget;

/**
 * <p>MapPanel interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface MapPanel {
    
    /**
     * <p>getWidget</p>
     *
     * @return a {@link com.google.gwt.user.client.ui.Widget} object.
     */
    public Widget getWidget();

    /**
     * <p>showLocationDetails</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param htmlTitle a {@link java.lang.String} object.
     * @param htmlContent a {@link java.lang.String} object.
     */
    public void showLocationDetails(String name, String htmlTitle, String htmlContent);

    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds();

    /**
     * <p>setBounds</p>
     *
     * @param locationBounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public void setBounds(GWTBounds locationBounds);

    /**
     * <p>placeMarker</p>
     *
     * @param marker a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
     */
    public void placeMarker(GWTMarkerState marker);
    
}
