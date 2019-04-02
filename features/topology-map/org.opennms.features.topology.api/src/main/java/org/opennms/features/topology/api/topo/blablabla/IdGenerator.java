/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo.blablabla;

import java.util.List;

import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Ref;

/**
 * This class generates an unique id.
 * The generated id has the format '<prefix><counter>' (e.g. v100).
 * So the generator must be initialized with a prefix and the initial counter.
 *
 * @author Markus von Rüden
 *
 */
abstract class IdGenerator {
    /**
     * The topology graph. It is needed to initialize the counter.
     */
    private final XXXGraph graph;
    /**
     * The prefix of the generated id. Must not be null.
     */
    private final String idPrefix;
    /**
     * The counter of the "next" (not current) id.
     */
    private int counter;
    /**
     * Defines if this generator is initialized or not.
     */
    private boolean initialized;

    protected IdGenerator(String idPrefix, XXXGraph graph) {
        this.idPrefix = idPrefix;
        this.graph = graph;
    }

    /**
     * Returns the next id in format '<prefix><counter>' (e.g. v100).
     *
     * If an entry with the generated id (see {@link #createId()}
     * already exists in {@link #graph} a new one is created.
     * This process is done until a key is created which is not already in {@link #graph}
     * @return The next id in format '<prefix><counter>' (e.g. v100).
     */
    public String getNextId() {
        try {
            initializeIfNeeded();
            while (!isValid(createId())) counter++;
            return createId();
        } finally {
            counter++;
        }
    }

    /**
     * Creates the id in format '<prefix><counter>' (e.g. v100)
     * @return the id in format '<prefix><counter>' (e.g. v100)
     */
    private String createId() {
        return idPrefix + counter;
    }

    /**
     * Returns the initial value of counter.
     *
     * Therefore the maximum number of each id from the {@link #getContent()} values are used.
     * A id can start with any prefix (or none) so only ids which starts with the same id as {@link #idPrefix} are considered.
     * If there is no matching content, 0 is returned.
     *
     * @return The initial value of counter.
     */
    private int getInitValue() {
        int max = 0;
        for (Ref ref : getContent()) {
            if (!ref.getId().startsWith(idPrefix)) continue;
            max = Math.max(max, extractIntegerFromId(ref.getId()));
        }
        return max;
    }

    /**
     * Returns true if the {@link #graph} does not contain a vertex id '<generatedId>', false otherwise.
     * @param generatedId The generated id
     * @return true if the {@link #graph} does not contain a vertex id '<generatedId>', false otherwise.
     */
    @SuppressWarnings("deprecation")
    private boolean isValid(String generatedId) {
        return !graph.containsVertexId(new DefaultVertexRef(graph.getNamespace(), generatedId));
    }

    public void reset() {
        counter = 0;
        initialized = false;
    }

    /**
     * Gets the integer value from the id.
     * If the id does not match to this generator or the id cannot be parsed as an integer 0 is returned.
     *
     * @param id the generated id. Should start with {@link #idPrefix}.
     * @return the integer value from the id. If the id does not match to this generator or the id cannot be parsed as an integer 0 is returned.
     */
    private int extractIntegerFromId(String id) {
        try {
            return Integer.parseInt(id.replaceAll(idPrefix, "").trim());
        } catch (NumberFormatException nfe) {
            return 0;
        } catch (IllegalArgumentException ilargex) {
            return 0;
        }
    }

    private void initializeIfNeeded() {
        if (!initialized) {
            counter = getInitValue();
            initialized = true;
        }
    }

    public abstract List<Ref> getContent();
}
