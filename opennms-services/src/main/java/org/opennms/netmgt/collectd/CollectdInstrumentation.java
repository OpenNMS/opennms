/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

/**
 * <p>CollectdInstrumentation interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectdInstrumentation {
    
    /**
     * <p>beginScheduleExistingInterfaces</p>
     */
    public void beginScheduleExistingInterfaces();
    /**
     * <p>endScheduleExistingInterfaces</p>
     */
    public void endScheduleExistingInterfaces();
    /**
     * <p>beginScheduleInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginScheduleInterfacesWithService(String svcName);
    /**
     * <p>endScheduleInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void endScheduleInterfacesWithService(String svcName);
    /**
     * <p>beginFindInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginFindInterfacesWithService(String svcName);
    /**
     * <p>endFindInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param count a int.
     */
    public void endFindInterfacesWithService(String svcName, int count);
    /**
     * <p>beginScheduleInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginScheduleInterface(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endScheduleInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void endScheduleInterface(int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectorInitialize</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginCollectorInitialize(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectorInitialize</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void endCollectorInitialize(int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectorRelease</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginCollectorRelease(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectorRelease</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void endCollectorRelease(int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectorCollect</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginCollectorCollect(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectorCollect</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void endCollectorCollect(int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectingServiceData</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectingServiceData</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void endCollectingServiceData(int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginPersistingServiceData</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void beginPersistingServiceData(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endPersistingServiceData</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void endPersistingServiceData(int nodeId, String ipAddress, String svcName);
    /**
     * <p>reportCollectionException</p>
     *
     * @param nodeid a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param e a {@link org.opennms.netmgt.collectd.CollectionException} object.
     */
    public void reportCollectionException(int nodeid, String ipAddress, String svcName, CollectionException e);
    

}
