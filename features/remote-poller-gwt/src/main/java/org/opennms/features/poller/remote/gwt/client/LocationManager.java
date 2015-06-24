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

import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;


/**
 * This interface specifies the model functions that allow data access to the
 * set of known {@link Location} objects that have been transmitted from the
 * server to the GWT client code.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationManager {
    
    /**
     * <p>initialize</p>
     * @param statuses 
     * @param application 
     */
    public abstract void initialize(Set<Status> statuses);
	
	/**
	 * <p>addLocationManagerInitializationCompleteEventHandler</p>
	 *
	 * @param handler a {@link org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander} object.
	 */
	public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler);
	/**
	 * <p>locationClicked</p>
	 */
	public void locationClicked();
	/**
	 * <p>applicationClicked</p>
	 */
	public void applicationClicked();
}
