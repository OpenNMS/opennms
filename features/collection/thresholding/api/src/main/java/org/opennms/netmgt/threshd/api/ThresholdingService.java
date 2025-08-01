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

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;

/**
 * Thresholding API Service.
 */
public interface ThresholdingService {

    /**
     * Creates a session to perform Thresholding against. 
     * 
     * The Session is keyed by the combination of nodeId, hostAddress and serviceName.
     * 
     * @param nodeId
     *            The Node Id.
     * @param hostAddress
     *            The Host IP Address.
     * @param serviceName
     *            The Service name.
     * @param serviceParameters
     *            Must not be null. Required by some existing {@link CollectionResource} objects to evaluate whether to apply thresholds when accepting a {@link CollectionSet}.
     *            If your {@link CollectionResource} does not require this, pass an empty {@link ServiceParameters} object.
     * @return A {@link ThresholdingSession}
     * @throws ThresholdInitializationException
     *             if there is an error creating the {@link ThresholdingSession} because of invalid Thresholding Configuration.
     */
    ThresholdingSession createSession(int nodeId, String hostAddress, String serviceName, ServiceParameters serviceParameters)
            throws ThresholdInitializationException;

    ThresholdingSetPersister getThresholdingSetPersister();
}
