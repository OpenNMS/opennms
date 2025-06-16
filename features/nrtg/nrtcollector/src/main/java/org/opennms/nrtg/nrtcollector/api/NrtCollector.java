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
package org.opennms.nrtg.nrtcollector.api;

import org.opennms.nrtg.api.model.CollectionJob;

/**
 * Technology free definition of a collection satellite.
 * <p>
 * a collection satellite receives {@link CollectionJob} from a collection
 * commander and collects the data for them. The result should be send back to
 * the commander via the same technology the job was received.
 * </p>
 *
 * @author Simon Walter
 */
public interface NrtCollector {

    /**
     * Starts the satellite. Must return after initialization and start.
     */
    void start();

    /**
     * Tells the main thread if the satellite instance is terminated.
     *
     * @return
     */
    boolean terminated();

    /**
     * Stops the nrtcollector and cleans up all the resources
     */
    void stop();
}
