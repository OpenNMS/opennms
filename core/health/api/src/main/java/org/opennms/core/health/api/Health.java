/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
