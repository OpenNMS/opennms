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

package org.opennms.plugins.elasticsearch.rest;

import org.junit.After;
import org.junit.Before;

import io.searchbox.client.JestClient;

public abstract class AbstractEventToIndexTest {

    protected JestClient jestClient;
    protected EventToIndex eventToIndex;

    @Before
    public void setUp() throws Exception {
        this.jestClient = new RestClientFactory("http://localhost:9200", "", "").createClient();
        this.eventToIndex = new EventToIndex(jestClient, 3);
    }

    @After
    public void tearDown() {
        if (jestClient != null) {
            jestClient.shutdownClient();
        }
        if (eventToIndex != null) {
            eventToIndex.close();
        }
    }

    protected EventToIndex getEventToIndex() {
        return eventToIndex;
    }
}
