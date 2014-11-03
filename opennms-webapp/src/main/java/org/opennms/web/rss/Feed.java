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

package org.opennms.web.rss;

import javax.servlet.ServletRequest;

/**
 * <p>Feed interface.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface Feed {

    /**
     * <p>getUrlBase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrlBase();
    /**
     * <p>setUrlBase</p>
     *
     * @param base a {@link java.lang.String} object.
     */
    public void setUrlBase(String base);
    
    /**
     * <p>getFeedType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFeedType();
    /**
     * <p>setFeedType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setFeedType(String type);
    
    /**
     * <p>getMaxEntries</p>
     *
     * @return a int.
     */
    public int getMaxEntries();
    /**
     * <p>setMaxEntries</p>
     *
     * @param maxEntries a int.
     */
    public void setMaxEntries(int maxEntries);

    /**
     * <p>getRequest</p>
     *
     * @return a {@link javax.servlet.ServletRequest} object.
     */
    public ServletRequest getRequest();
    /**
     * <p>setRequest</p>
     *
     * @param request a {@link javax.servlet.ServletRequest} object.
     */
    public void setRequest(ServletRequest request);
    
    /**
     * <p>render</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String render();
    
}
