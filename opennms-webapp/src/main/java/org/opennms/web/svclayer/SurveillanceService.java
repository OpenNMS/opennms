/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * 
 * Created: September 9, 2006
 * Modifications:
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 *
 * Class designed for gathering Aggreate Status of nodes to be displayed
 * in a cross sectional view of categories.  This service provides the objects
 * requried for a view used by a surveillance/operations team within a NOC.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly=true)
public interface SurveillanceService {

    /**
     * <p>createSurveillanceTable</p>
     *
     * @param surveillanceViewName a {@link java.lang.String} object.
     * @param progressMonitor a {@link org.opennms.web.svclayer.ProgressMonitor} object.
     * @return a {@link org.opennms.web.svclayer.SimpleWebTable} object.
     */
    public SimpleWebTable createSurveillanceTable(String surveillanceViewName, ProgressMonitor progressMonitor);
    
    /**
     * <p>getHeaderRefreshSeconds</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getHeaderRefreshSeconds(String viewName);

    /**
     * <p>isViewName</p>
     *
     * @param viewName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isViewName(String viewName);
    
    /**
     * <p>getViewNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getViewNames();
}
