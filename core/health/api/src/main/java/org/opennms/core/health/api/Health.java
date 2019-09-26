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

/**
 * Represents the "Health", by holding a list of {@link Response}s.
 * It allows accessing the responses and also provides some helper methods (e.g. to get the worst response).
 *
 * @author mvrueden
 */
public class Health {

    private List<Response> responses = new ArrayList<>();
    private String errorMessage;

    public Health withResponse(Response response) {
        add(response);
        return this;
    }

    public boolean isSuccess() {
        if (responses.isEmpty() && errorMessage != null) {
            return false;
        }
        return responses.stream().filter(r -> r.getStatus() != Status.Success).count() == 0;
    }

    public Optional<Response> getWorst() {
        if (responses.isEmpty()) {
            return Optional.empty();
        }
        return responses.stream()
                .sorted(Comparator.comparingInt(response -> -1 * response.getStatus().ordinal()))
                .findFirst();
    }

    public void add(Response response) {
        this.responses.add(response);
    }

    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<Response> getResponses() {
        return new ArrayList<>(responses);
    }
}
