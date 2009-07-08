/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
    public void getSurveillanceData(AsyncCallback cb);
    
    public void getAlarmsForSet(SurveillanceSet set, AsyncCallback cb);
    
    public void getNotificationsForSet(SurveillanceSet set, AsyncCallback cb);
    
    public void getNodeNames(SurveillanceSet set, AsyncCallback cb);
    
    public void getResources(SurveillanceSet set, AsyncCallback cb);

    public void getChildResources(String resourceId, AsyncCallback cb);

    public void getPrefabGraphs(String resourceId, AsyncCallback cb);
    
    public void getRtcForSet(SurveillanceSet set, AsyncCallback cb);
}
