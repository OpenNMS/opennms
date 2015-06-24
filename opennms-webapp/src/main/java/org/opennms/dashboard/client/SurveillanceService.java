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

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * <p>SurveillanceService interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface SurveillanceService extends RemoteService {

    /**
     * <p>getSurveillanceData</p>
     *
     * @return a {@link org.opennms.dashboard.client.SurveillanceData} object.
     */
    public SurveillanceData getSurveillanceData();
    
    /**
     * <p>getAlarmsForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @return an array of {@link org.opennms.dashboard.client.Alarm} objects.
     */
    public Alarm[] getAlarmsForSet(SurveillanceSet set);
    
    /**
     * <p>getNotificationsForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @return an array of {@link org.opennms.dashboard.client.Notification} objects.
     */
    public Notification[] getNotificationsForSet(SurveillanceSet set);
    
    /**
     * <p>getNodeNames</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getNodeNames(SurveillanceSet set);
    
    /**
     * <p>getResources</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[][] getResources(SurveillanceSet set);
    
    /**
     * <p>getChildResources</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[][] getChildResources(String resourceId);
    
    /**
     * <p>getPrefabGraphs</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public String[][] getPrefabGraphs(String resourceId);
    
    /**
     * <p>getRtcForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @return an array of {@link org.opennms.dashboard.client.NodeRtc} objects.
     */
    public NodeRtc[] getRtcForSet(SurveillanceSet set);
}
