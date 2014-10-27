/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

/**
 * <p>FilterManager interface.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public interface FilterManager {
    
    /** Constant <code>AUTH_FILTER_NAME="authorizedOnly"</code> */
    public static final String AUTH_FILTER_NAME = "authorizedOnly";
    
    /**
     * <p>enableAuthorizationFilter</p>
     *
     * @param authorizationGroups an array of {@link java.lang.String} objects.
     */
    public void enableAuthorizationFilter(String[] authorizationGroups);
    
    /**
     * <p>disableAuthorizationFilter</p>
     */
    public void disableAuthorizationFilter();

    public String[] getAuthorizationGroups();

    public boolean isEnabled();

}
