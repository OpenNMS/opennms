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
package org.opennms.netmgt.threshd.api;

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.xml.event.Event;

public interface ThresholdingSession extends AutoCloseable {

    /**
     * Accepts a {@link CollectionSet} for threshold evaluation. The service will send {@link Event}s if Thresholds are triggered or re-armed.
     * 
     * @param collectionSet
     * @throws ThresholdInitializationException
     *             if the Thresholding Configuration has not yet been initialized ot there is an error initializing it. 
     *             I.E. reading as parsing the configuration files.
     */
    void accept(CollectionSet collectionSet) throws ThresholdInitializationException;

    ThresholdingSessionKey getKey();
    
    BlobStore getBlobStore();

    /**
     * @return true if we are thresholding in a distributed environment (i.e. Sentinel) false otherwise (i.e. OpenNMS)
     */
    boolean isDistributed();
    
    ThresholdStateMonitor getThresholdStateMonitor();
}
