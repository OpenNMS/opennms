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

package org.opennms.web.svclayer.model;

/**
 * <p>DistributedStatusDetailsCommand class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DistributedStatusDetailsCommand {
    private String m_location;
    private String m_application;
    
    /**
     * <p>getApplication</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplication() {
        return m_application;
    }
    /**
     * <p>setApplication</p>
     *
     * @param application a {@link java.lang.String} object.
     */
    public void setApplication(String application) {
        m_application = application;
    }
    /**
     * <p>getLocation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocation() {
        return m_location;
    }
    /**
     * <p>setLocation</p>
     *
     * @param location a {@link java.lang.String} object.
     */
    public void setLocation(String location) {
        m_location = location;
    }
    
}
