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
package org.opennms.web.rest.v2.model;

import java.util.List;

/**
 * Replaces an application's member services and perspective locations. A list
 * left null keeps the current members of that kind.
 */
public class ApplicationMembersUpdate {

    private List<Integer> serviceIds;

    private List<String> perspectiveLocations;

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(final List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public List<String> getPerspectiveLocations() {
        return perspectiveLocations;
    }

    public void setPerspectiveLocations(final List<String> perspectiveLocations) {
        this.perspectiveLocations = perspectiveLocations;
    }
}
