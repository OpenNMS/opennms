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
package org.opennms.netmgt.collection.core;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.collection.api.CollectionInstrumentation;
import org.opennms.netmgt.collection.api.CollectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultCollectdInstrumentation implements CollectionInstrumentation {
    public static final Logger LOG = LoggerFactory.getLogger(DefaultCollectdInstrumentation.class);

    @Override
    public void beginScheduleExistingInterfaces() {
        log("scheduleExistingInterfaces: begin");
    }

    @Override
    public void endScheduleExistingInterfaces() {
        log("scheduleExistingInterfaces: end");
    }

    @Override
    public void beginScheduleInterfacesWithService(final String svcName) {
        log("scheduleInterfacesWithService: begin: {}", svcName);
    }

    @Override
    public void endScheduleInterfacesWithService(final String svcName) {
        log("scheduleInterfacesWithService: end: {}", svcName);
    }

    @Override
    public void beginFindInterfacesWithService(final String svcName) {
        log("scheduleFindInterfacesWithService: begin: {}", svcName);
    }

    @Override
    public void endFindInterfacesWithService(final String svcName, final int count) {
        log("scheduleFindInterfacesWithService: end: {}. found {} interfaces.", svcName, count);
    }

    @Override
    public void beginCollectingServiceData(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.collect: collectData: begin: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void endCollectingServiceData(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.collect: collectData: end: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void beginCollectorCollect(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.collect: begin:{}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void endCollectorCollect(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.collect: end:{}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void beginCollectorRelease(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.release: begin: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void endCollectorRelease(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.release: end: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void beginPersistingServiceData(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.collect: persistDataQueueing: begin: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void endPersistingServiceData(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.collect: persistDataQueueing: end: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void beginCollectorInitialize(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.initialize: begin: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void endCollectorInitialize(final String packageName, final int nodeId, final String ipAddress, final String svcName) {
        log("collector.initialize: end: {}/{}/{}/{}", packageName, nodeId, ipAddress, svcName);
    }

    @Override
    public void beginScheduleInterface(final int nodeId, final String ipAddress, final String svcName) {
        log("scheduleInterfaceWithService: begin: {}/{}/{}", nodeId, ipAddress, svcName);
    }

    @Override
    public void endScheduleInterface(final int nodeId, final String ipAddress, final String svcName) {
        log("scheduleInterfaceWithService: end: {}/{}/{}", nodeId, ipAddress, svcName);
    }

    @Override
    public void reportCollectionException(final String packageName, final int nodeId, final String ipAddress, final String svcName, final CollectionException e) {
        log("collector.collect: error: {}/{}/{}/{}: {}", packageName, nodeId, ipAddress, svcName, e.getMessage());
    }

    private void log(final String msg, final Object... args) {
        Logging.withPrefix("instrumentation", new Runnable() {
            @Override public void run() {
                LOG.info(msg, args);
            }
        });
        
    }

}
