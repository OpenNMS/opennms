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
 * <p>ApplicationSelectedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationSelectedEvent extends GwtEvent<ApplicationSelectedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<ApplicationSelectedEventHandler> TYPE = new Type<>();
    private String m_applicationName;
    
    /**
     * <p>Constructor for ApplicationSelectedEvent.</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     */
    public ApplicationSelectedEvent(final String applicationName) {
        m_applicationName = applicationName;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(ApplicationSelectedEventHandler handler) {
        handler.onApplicationSelected(this);
    }

    /** {@inheritDoc} */
    @Override
    public Type<ApplicationSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * <p>getApplicationname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplicationname() {
        return m_applicationName;
    }

}
