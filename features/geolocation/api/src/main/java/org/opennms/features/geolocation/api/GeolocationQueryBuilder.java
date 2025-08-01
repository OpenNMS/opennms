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
package org.opennms.features.geolocation.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class GeolocationQueryBuilder {

    private GeolocationQuery query = new GeolocationQuery();

    public GeolocationQueryBuilder withStatusCalculationStrategy(StatusCalculationStrategy statusCalculationStrategy) {
        query.setStatusCalculationStrategy(statusCalculationStrategy);
        return this;
    }

    public GeolocationQueryBuilder withLocation(String location) {
        query.setLocation(location);
        return this;
    }


    public GeolocationQueryBuilder withIncludeAcknowledgedAlarms(boolean includeAcknowledgedAlarms) {
        query.setIncludeAcknowledgedAlarms(includeAcknowledgedAlarms);
        return this;
    }

    public GeolocationQueryBuilder withSeverity(GeolocationSeverity severity) {
        query.setSeverity(severity);
        return this;
    }

    public GeolocationQueryBuilder withNodeIds(Integer... nodeIds) {
        withNodeIds(Arrays.asList(nodeIds));
        return this;
    }

    public GeolocationQueryBuilder withNodeIds(Collection<Integer> nodeIds) {
        query.setNodeIds(new ArrayList<>(nodeIds));
        return this;
    }

    public GeolocationQuery build() {
        return query;
    }

}
