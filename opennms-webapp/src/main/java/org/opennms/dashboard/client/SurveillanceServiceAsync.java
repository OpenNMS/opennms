/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous interface for SurveillanceService.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface SurveillanceServiceAsync {
    /**
     * <p>getSurveillanceData</p>
     *
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getSurveillanceData(AsyncCallback cb);
    
    /**
     * <p>getAlarmsForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getAlarmsForSet(SurveillanceSet set, AsyncCallback cb);
    
    /**
     * <p>getNotificationsForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getNotificationsForSet(SurveillanceSet set, AsyncCallback cb);
    
    /**
     * <p>getNodeNames</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getNodeNames(SurveillanceSet set, AsyncCallback cb);
    
    /**
     * <p>getResources</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getResources(SurveillanceSet set, AsyncCallback cb);

    /**
     * <p>getChildResources</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getChildResources(String resourceId, AsyncCallback cb);

    /**
     * <p>getPrefabGraphs</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getPrefabGraphs(String resourceId, AsyncCallback cb);
    
    /**
     * <p>getRtcForSet</p>
     *
     * @param set a {@link org.opennms.dashboard.client.SurveillanceSet} object.
     * @param cb a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    public void getRtcForSet(SurveillanceSet set, AsyncCallback cb);
}
