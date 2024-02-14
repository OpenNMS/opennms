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
package org.opennms.core.health.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Represents the "Health", by holding a list of {@link HealthCheck}s and their {@link Response}s.
 * It allows accessing the responses and also provides some helper methods (e.g. to get the worst response).
 *
 * @author mvrueden
 */
public class Health {

    private final List<Pair<HealthCheck, Response>> responses;

    public Health() {
        responses = new ArrayList<>();
    }

    public Health(List<Pair<HealthCheck, Response>> responses) {
        this.responses = responses;
    }

    public Health withResponse(HealthCheck healthCheck, Response response) {
        add(healthCheck, response);
        return this;
    }

    public boolean isSuccess() {
        return responses.stream().allMatch(r -> r.getRight().getStatus() == Status.Success);
    }

    public Optional<Pair<HealthCheck, Response>> getWorst() {
        return responses.stream().max(Comparator.comparing(p -> p.getRight().getStatus()));
    }

    public void add(HealthCheck healthCheck, Response response) {
        this.responses.add(Pair.of(healthCheck, response));
    }

    public List<Pair<HealthCheck, Response>> getResponses() {
        return new ArrayList<>(responses);
    }
}
