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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.web.rest.v2.model.HttpAttribDto;
import org.opennms.web.rest.v2.model.HttpCollectionConfigDto;
import org.opennms.web.rest.v2.model.HttpCollectionDto;
import org.opennms.web.rest.v2.model.HttpHeaderDto;
import org.opennms.web.rest.v2.model.HttpRrdDto;
import org.opennms.web.rest.v2.model.HttpUriDto;
import org.opennms.web.rest.v2.model.HttpUrlDto;
import org.opennms.web.rest.v2.model.ParameterDto;

public class HttpCollectionConfigValidatorTest {

    @Test
    public void acceptsAValidCollection() {
        assertNull(HttpCollectionConfigValidator.validateCollection(collection()));
    }

    @Test
    public void acceptsAUriWithoutAttributesOrMatches() {
        final HttpCollectionDto collection = collection();
        collection.getUris().get(0).setAttributes(null);
        collection.getUris().get(0).getUrl().setMatches(null);
        assertNull(HttpCollectionConfigValidator.validateCollection(collection));
    }

    @Test
    public void requiresAName() {
        final HttpCollectionDto collection = collection();
        collection.setName(" ");
        assertEquals("HTTP collection name is required.", HttpCollectionConfigValidator.validateCollection(collection));
    }

    @Test
    public void checksTheRrd() {
        final HttpCollectionDto collection = collection();
        collection.getRrd().setStep(0);
        assertMessage(collection, "rrd step must be a positive number of seconds.");

        collection.getRrd().setStep(300);
        collection.getRrd().setRras(List.of());
        assertMessage(collection, "at least one RRA is required.");

        collection.getRrd().setRras(List.of("RRA:AVERAGE:0.5:1:2016", "RRA:SUM:0.5:1:10"));
        assertMessage(collection, "RRA 'RRA:SUM:0.5:1:10' must have the form");
    }

    @Test
    public void checksTheUrl() {
        final HttpCollectionDto collection = collection();
        final HttpUrlDto url = collection.getUris().get(0).getUrl();

        url.setPath("");
        assertMessage(collection, "URI at index 0: url path is required.");
        url.setPath("/");

        url.setMethod("PUT");
        assertMessage(collection, "method 'PUT' must be one of GET, POST.");
        url.setMethod("POST");

        url.setHttpVersion("2");
        assertMessage(collection, "httpVersion '2' must be one of 1.0, 1.1.");
        url.setHttpVersion(null);

        url.setPort(70000);
        assertMessage(collection, "port 70000 must be between 1 and 65535.");
        url.setPort(8080);

        url.setResponseRange("399-100");
        assertMessage(collection, "responseRange '399-100' must have the form low-high");
        url.setResponseRange("200-299");

        url.setMatches("([0-9]+");
        assertMessage(collection, "matches is not a valid regular expression");
        url.setMatches("Count: ([0-9]+)");

        final ParameterDto parameter = new ParameterDto();
        parameter.setKey("q");
        url.setParameters(List.of(parameter));
        assertMessage(collection, "every parameter needs a key and a value.");
        url.setParameters(null);

        url.setHeaders(List.of(new HttpHeaderDto()));
        assertMessage(collection, "every header needs a name and a value.");
        url.setHeaders(null);

        assertNull(HttpCollectionConfigValidator.validateCollection(collection));
    }

    @Test
    public void compilesMatchesWithTheSelectedFlags() {
        final HttpCollectionDto collection = collection();
        final HttpUrlDto url = collection.getUris().get(0).getUrl();
        // A comment only parses with the COMMENTS flag; with LITERAL the expression has no groups at all.
        url.setMatches("(\\d+) # the count");
        url.setComments(true);
        assertNull(HttpCollectionConfigValidator.validateCollection(collection));

        url.setComments(null);
        url.setLiteral(true);
        assertMessage(collection, "matchGroup 1 does not exist; the matches expression has 0 groups.");
    }

    @Test
    public void checksTheAttributes() {
        final HttpCollectionDto collection = collection();
        final HttpAttribDto attrib = collection.getUris().get(0).getAttributes().get(0);

        attrib.setAlias("aVeryLongAliasNameOver19");
        assertMessage(collection, "attribute at index 0: alias 'aVeryLongAliasNameOver19' is longer than 19 characters.");
        attrib.setAlias("count");

        attrib.setMatchGroup(0);
        assertMessage(collection, "matchGroup must be 1 or greater.");
        attrib.setMatchGroup(2);
        assertMessage(collection, "matchGroup 2 does not exist; the matches expression has 1 group.");
        attrib.setMatchGroup(1);

        attrib.setType("timeticks");
        assertMessage(collection, "type 'timeticks' must be one of gauge, counter or string.");
        attrib.setType("COUNTER32");
        assertNull(HttpCollectionConfigValidator.validateCollection(collection));

        final HttpAttribDto twin = new HttpAttribDto();
        twin.setAlias("count");
        twin.setMatchGroup(1);
        twin.setType("gauge");
        collection.getUris().get(0).getAttributes().add(twin);
        assertMessage(collection, "attribute alias 'count' is used more than once.");
    }

    @Test
    public void refusesDuplicateUriNames() {
        final HttpCollectionDto collection = collection();
        collection.getUris().add(collection().getUris().get(0));
        assertMessage(collection, "URI name 'status' is used more than once.");
    }

    @Test
    public void refusesCollectionsWhoseNamesDifferOnlyInCase() {
        final HttpCollectionConfigDto config = new HttpCollectionConfigDto();
        config.setRrdRepository("/tmp/");
        final HttpCollectionDto other = collection();
        other.setName("STATUS-COLLECTION");
        config.setCollections(List.of(collection(), other));
        assertEquals("The configuration contains HTTP collection 'STATUS-COLLECTION' more than once.",
                HttpCollectionConfigValidator.validateConfig(config));

        config.setRrdRepository(null);
        assertEquals("rrdRepository is required.", HttpCollectionConfigValidator.validateConfig(config));
    }

    private static void assertMessage(final HttpCollectionDto collection, final String expected) {
        final String message = HttpCollectionConfigValidator.validateCollection(collection);
        assertTrue("expected '" + expected + "' in '" + message + "'", message != null && message.contains(expected));
    }

    static HttpCollectionDto collection() {
        final HttpRrdDto rrd = new HttpRrdDto();
        rrd.setStep(300);
        rrd.setRras(List.of("RRA:AVERAGE:0.5:1:2016"));

        final HttpUrlDto url = new HttpUrlDto();
        url.setPath("/status");
        url.setMatches("Count: ([0-9]+)");

        final HttpAttribDto attrib = new HttpAttribDto();
        attrib.setAlias("count");
        attrib.setMatchGroup(1);
        attrib.setType("gauge");

        final HttpUriDto uri = new HttpUriDto();
        uri.setName("status");
        uri.setUrl(url);
        uri.setAttributes(new ArrayList<>(List.of(attrib)));

        final HttpCollectionDto collection = new HttpCollectionDto();
        collection.setName("status-collection");
        collection.setRrd(rrd);
        collection.setUris(new ArrayList<>(List.of(uri)));
        return collection;
    }
}
