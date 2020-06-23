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

package org.opennms.netmgt.collection.api;


/**
 * <p>CollectdInstrumentation interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionInstrumentation {
    
    /**
     * <p>beginScheduleExistingInterfaces</p>
     */
    void beginScheduleExistingInterfaces();
    /**
     * <p>endScheduleExistingInterfaces</p>
     */
    void endScheduleExistingInterfaces();
    /**
     * <p>beginScheduleInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    void beginScheduleInterfacesWithService(String svcName);
    /**
     * <p>endScheduleInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    void endScheduleInterfacesWithService(String svcName);
    /**
     * <p>beginFindInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    void beginFindInterfacesWithService(String svcName);
    /**
     * <p>endFindInterfacesWithService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param count a int.
     */
    void endFindInterfacesWithService(String svcName, int count);
    /**
     * <p>beginScheduleInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void beginScheduleInterface(int nodeId, String ipAddress, String svcName);
    /**
     * <p>endScheduleInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void endScheduleInterface(int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectorInitialize</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void beginCollectorInitialize(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectorInitialize</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void endCollectorInitialize(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectorRelease</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void beginCollectorRelease(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectorRelease</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void endCollectorRelease(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectorCollect</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void beginCollectorCollect(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectorCollect</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void endCollectorCollect(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginCollectingServiceData</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void beginCollectingServiceData(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>endCollectingServiceData</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void endCollectingServiceData(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>beginPersistingServiceData</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void beginPersistingServiceData(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>endPersistingServiceData</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    void endPersistingServiceData(String packageName, int nodeId, String ipAddress, String svcName);
    /**
     * <p>reportCollectionException</p>
     *
     * @param packageName a {@link java.lang.String} object.
     * @param nodeid a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param e a {@link org.opennms.netmgt.collection.api.CollectionException} object.
     */
    void reportCollectionException(String packageName, int nodeid, String ipAddress, String svcName, CollectionException e);
    

}
