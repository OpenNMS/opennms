/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao;

import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.config.siteStatusViews.Views;

/**
 * <p>SiteStatusViewConfigDao interface.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface SiteStatusViewConfigDao {
    
    /**
     * <p>getViews</p>
     *
     * @return a {@link org.opennms.netmgt.config.siteStatusViews.Views} object.
     */
    Views getViews();
    
    /**
     * <p>getView</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.siteStatusViews.View} object.
     */
    View getView(String viewName);
    
    /**
     * <p>getDefaultView</p>
     *
     * @return a {@link org.opennms.netmgt.config.siteStatusViews.View} object.
     */
    View getDefaultView();

}
