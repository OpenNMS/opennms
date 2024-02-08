/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
