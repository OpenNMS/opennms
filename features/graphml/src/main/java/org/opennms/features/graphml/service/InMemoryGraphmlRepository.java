/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.graphml.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.graphdrawing.graphml.GraphmlType;

public class InMemoryGraphmlRepository implements GraphmlRepository {

    private final Map<String, GraphmlType> repository = new HashMap<>();

    @Override
    public GraphmlType findByName(String name) throws IOException {
        Objects.requireNonNull(name);
        validateExists(name);
        return repository.get(name);
    }

    @Override
    public void save(String name, String label, GraphmlType graphmlType) throws IOException {
        if (exists(name)) {
            throw new IOException(name + " already exists");
        }
        repository.put(name, graphmlType);
    }

    @Override
    public void delete(String name) throws IOException {
        validateExists(name);
        repository.remove(name);
    }

    @Override
    public boolean exists(String name) {
        return repository.get(name) != null;
    }

    private void validateExists(String name) {
        if (!exists(name)) {
            throw new NoSuchElementException("No GraphML file found with name  " + name);
        }
    }
}
