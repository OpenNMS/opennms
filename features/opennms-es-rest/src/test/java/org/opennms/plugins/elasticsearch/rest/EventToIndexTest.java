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
package org.opennms.plugins.elasticsearch.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.opennms.plugins.elasticsearch.rest.EventToIndex.isOID;

import java.util.Collections;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class EventToIndexTest {

    @Test
    public void verifyIsOID() {
        assertThat(isOID(".3"), is(true));
        assertThat(isOID(".3.1.2"), is(true));
        assertThat(isOID("..3.."), is(false));
        assertThat(isOID("192.168.0.1"), is(false));
        assertThat(isOID("nodeLabel"), is(false));
    }

    @Test
    public void testHandleParameters() {
        @SuppressWarnings("resource")
        final EventToIndex etoi = new EventToIndex(mock(JestClientWithCircuitBreaker.class), 1);
        etoi.setGroupOidParameters(true);

        final Event e = new EventBuilder("opennms/fake-uei", "source").addParam(".3.1.2", "oid-param").addParam("nodeLabel", "fake-node").getEvent();
        e.setParmCollection(Collections.unmodifiableList(e.getParmCollection()));
        final JSONObject json = new JSONObject();
        etoi.handleParameters(e, json);
        assertEquals(2, e.getParmCollection().size());
        assertNotNull(json.get("p_oids"));
        assertEquals(2, json.keySet().size());
    }
}
