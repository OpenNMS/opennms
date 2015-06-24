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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous interface for SurveillanceService.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public interface SurveillanceServiceAsync {
    /**
     * <p>getSurveillanceData</p>
     *
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getSurveillanceData(AsyncCallback<SurveillanceData> cb);
    
    /**
     * <p>getAlarmsForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getAlarmsForSet(SurveillanceSet set, AsyncCallback<Alarm[]> cb);
    
    /**
     * <p>getNotificationsForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getNotificationsForSet(SurveillanceSet set, AsyncCallback<Notification[]> cb);
    
    /**
     * <p>getNodeNames</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getNodeNames(SurveillanceSet set, AsyncCallback<String[]> cb);
    
    /**
     * <p>getResources</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getResources(SurveillanceSet set, AsyncCallback<String[][]> cb);

    /**
     * <p>getChildResources</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getChildResources(String resourceId, AsyncCallback<String[][]> cb);

    /**
     * <p>getPrefabGraphs</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getPrefabGraphs(String resourceId, AsyncCallback<String[][]> cb);
    
    /**
     * <p>getRtcForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getRtcForSet(SurveillanceSet set, AsyncCallback<NodeRtc[]> cb);
}
