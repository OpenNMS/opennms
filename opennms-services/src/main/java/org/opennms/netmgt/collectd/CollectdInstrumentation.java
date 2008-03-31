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
 * Created: July 20, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.collectd;

public interface CollectdInstrumentation {
    
    public void beginScheduleExistingInterfaces();
    public void endScheduleExistingInterfaces();
    public void beginScheduleInterfacesWithService(String svcName);
    public void endScheduleInterfacesWithService(String svcName);
    public void beginFindInterfacesWithService(String svcName);
    public void endFindInterfacesWithService(String svcName, int count);
    public void beginScheduleInterface(int nodeId, String ipAddress, String svcName);
    public void endScheduleInterface(int nodeId, String ipAddress, String svcName);
    public void beginCollectorInitialize(int nodeId, String ipAddress, String svcName);
    public void endCollectorInitialize(int nodeId, String ipAddress, String svcName);
    public void beginCollectorRelease(int nodeId, String ipAddress, String svcName);
    public void endCollectorRelease(int nodeId, String ipAddress, String svcName);
    public void beginCollectorCollect(int nodeId, String ipAddress, String svcName);
    public void endCollectorCollect(int nodeId, String ipAddress, String svcName);
    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName);
    public void endCollectingServiceData(int nodeId, String ipAddress, String svcName);
    public void beginPersistingServiceData(int nodeId, String ipAddress, String svcName);
    public void endPersistingServiceData(int nodeId, String ipAddress, String svcName);
    public void reportCollectionError(int nodeid, String ipAddress, String svcName, CollectionError e);
    

}
