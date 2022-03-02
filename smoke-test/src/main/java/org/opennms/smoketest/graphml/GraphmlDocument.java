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

package org.opennms.smoketest.graphml;

import java.io.InputStream;
import java.util.Objects;

import org.opennms.smoketest.utils.RestClient;

public class GraphmlDocument {

    private final String name;
    private final String resource;

    public GraphmlDocument(final String graphName, final String graphResource) {
        this.name = Objects.requireNonNull(graphName);
        this.resource = Objects.requireNonNull(graphResource);
        Objects.requireNonNull(getResourceAsStream());
    }

    public void create(final RestClient client) {
        if (client.getGraphML(name).getStatus() == 404) {
            client.sendGraphML(name, getResourceAsStream());
        }
    }

    public void delete(final RestClient client) {
        if (client.getGraphML(name).getStatus() != 404) {
            client.deleteGraphML(name);
        }
    }

    public boolean exists(RestClient client) {
        return client.getGraphML(name).getStatus() == 200;
    }

    private InputStream getResourceAsStream() {
        return getClass().getResourceAsStream(resource);
    }
}
