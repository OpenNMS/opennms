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

package org.opennms.netmgt.telemetry.daemon;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;


public class LocationPublisherManager {

    @Autowired
    private  TwinPublisher twinPublisher;

    private final ConcurrentHashMap<String, LocationPublisher> publishers = new ConcurrentHashMap<>();

    public LocationPublisher getOrCreate(String location) {
        return publishers.computeIfAbsent(location, loc -> new LocationPublisher(loc, twinPublisher));
    }

    public void removeIfEmpty(String location) {
        LocationPublisher lp = publishers.get(location);
        if (lp != null && !lp.hasConfigs()) {
            publishers.remove(location, lp);
        }
    }

    public void forceCloseAll() {
        publishers.forEach((loc, lp) -> lp.forceClose());
        publishers.clear();
    }
}