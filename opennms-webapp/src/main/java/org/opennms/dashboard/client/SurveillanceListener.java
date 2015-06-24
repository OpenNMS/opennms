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

package org.opennms.dashboard.client;

/**
 * <p>SurveillanceListener interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface SurveillanceListener {
    
    /**
     * <p>onAllClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     */
    public void onAllClicked(Dashlet viewer);
    
    /**
     * <p>onSurveillanceGroupClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param group a {@link org.opennms.dashboard.client.SurveillanceGroup} object.
     */
    public void onSurveillanceGroupClicked(Dashlet viewer, SurveillanceGroup group);
    
    /**
     * <p>onIntersectionClicked</p>
     *
     * @param viewer a {@link org.opennms.dashboard.client.Dashlet} object.
     * @param intersection a {@link org.opennms.dashboard.client.SurveillanceIntersection} object.
     */
    public void onIntersectionClicked(Dashlet viewer, SurveillanceIntersection intersection);

}
