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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Pinpoint tests for {@link DataCollectionConfRestService#parseDataCollectionFile}.
 *
 * The upload path must accept both XML that declares the OpenNMS datacollection
 * namespace and XML that omits it (matching the client-side validator's
 * permissive behavior). XML that declares a wrong namespace must still be
 * rejected.
 */
public class DataCollectionConfRestServiceParseTest {

    private final DataCollectionConfRestService svc = new DataCollectionConfRestService();

    @Test
    public void acceptsCorrectNamespace() throws Exception {
        final String xml =
                "<?xml version=\"1.0\"?>" +
                "<datacollection-group xmlns=\"http://xmlns.opennms.org/xsd/config/datacollection\" name=\"NamespacedGroup\"/>";

        final DatacollectionGroup g = parse(xml);
        assertEquals("NamespacedGroup", g.getName());
    }

    @Test
    public void acceptsNamespaceLessXmlByInjectingDefault() throws Exception {
        // Customer files in the wild sometimes omit xmlns. Without the filter,
        // JaxbUtils.unmarshal would reject this; with it, we treat it as the
        // OpenNMS datacollection namespace.
        final String xml =
                "<?xml version=\"1.0\"?>" +
                "<datacollection-group name=\"BareGroup\"/>";

        final DatacollectionGroup g = parse(xml);
        assertEquals("BareGroup", g.getName());
    }

    @Test
    public void rejectsWrongNamespace() {
        final String xml =
                "<?xml version=\"1.0\"?>" +
                "<datacollection-group xmlns=\"http://example.com/wrong\" name=\"WrongNs\"/>";

        try {
            parse(xml);
            fail("Expected unmarshal failure for wrong namespace");
        } catch (Exception expected) {
            // JAXB should refuse since our filter only injects when uri is empty
        }
    }

    private DatacollectionGroup parse(final String xml) throws Exception {
        return svc.parseDataCollectionFile(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
