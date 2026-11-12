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
package org.opennms.features.config.service.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.io.Resources;

/**
 * The HTTP collector configuration is stored in Config Management as JSON. It enters CM through
 * the upgrade's importConfig (XML converted to JSON from the XSD) and is then read and written with
 * {@link ConfigConvertUtil} (Jackson on the JAXB model). Both sides must agree on the JSON shape,
 * and anything the DAO writes must pass the schema validation CM applies on every update.
 */
public class HttpDatacollectionJsonCompatibilityTest {
    private static final String XSD = "http-datacollection-config.xsd";
    private static final String ROOT_ELEMENT = "http-datacollection-config";

    private static final ConfigDefinition DEFINITION = XsdHelper.buildConfigDefinition("http-datacollection-config", XSD, ROOT_ELEMENT, "/rest/cm", false);

    @Test
    public void xmlImportedThroughTheSchemaReadsBackUnchanged() throws IOException {
        final String xml = resource("http-datacollection-config.xml");
        final String importedJson = XsdHelper.getConverter(DEFINITION).xmlToJson(xml);

        final HttpDatacollectionConfig fromXml = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, xml);
        final HttpDatacollectionConfig fromJson = ConfigConvertUtil.jsonToObject(importedJson, HttpDatacollectionConfig.class);

        assertThat(fromJson.getHttpCollection().size(), equalTo(2));
        assertThat(fromJson.getHttpCollection().get(0).getUris().get(0).getUrl().getParameters().size(), equalTo(2));
        assertThat(fromJson.getHttpCollection().get(0).getUris().get(0).getUrl().getHeaders().size(), equalTo(2));
        assertThat(fromJson.getHttpCollection().get(1).getUris().get(0).getAttributes().get(0).getType(), equalTo(AttributeType.COUNTER));
        assertThat(fromJson, equalTo(fromXml));
        assertThat(ConfigConvertUtil.jsonToObject(ConfigConvertUtil.objectToJson(fromJson), HttpDatacollectionConfig.class), equalTo(fromXml));
    }

    @Test
    public void emptyDefaultConfigImports() throws IOException {
        final String xml = "<http-datacollection-config xmlns=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" rrdRepository=\"/opt/opennms/share/rrd/snmp/\"/>";
        final String importedJson = XsdHelper.getConverter(DEFINITION).xmlToJson(xml);
        DEFINITION.validate(importedJson);

        final HttpDatacollectionConfig fromJson = ConfigConvertUtil.jsonToObject(importedJson, HttpDatacollectionConfig.class);
        assertThat(fromJson, equalTo(JaxbUtils.unmarshal(HttpDatacollectionConfig.class, xml)));
        assertThat(fromJson.getRrdRepository(), equalTo("/opt/opennms/share/rrd/snmp/"));
        DEFINITION.validate(ConfigConvertUtil.objectToJson(fromJson));
    }

    /**
     * The import fills in empty wrappers such as {@code "parameters":{"parameter":[]}} for URIs without
     * parameters. Read back, they must not marshal as an empty {@code <parameters/>}: the schema requires at
     * least one child, so the XML download and the collector's marshalling for Minion would fail.
     */
    @Test
    public void importedConfigMarshalsToValidXml() throws IOException {
        // As stored by the configuration manager, which fills in the empty wrappers of the "minimal" URI.
        final String storedJson = "{\"rrdRepository\":\"/rrd/\",\"http-collection\":[{\"name\":\"c\","
                + "\"rrd\":{\"step\":300,\"rra\":[\"RRA:AVERAGE:0.5:1:10\"]},"
                + "\"uris\":{\"uri\":[{\"name\":\"minimal\",\"url\":{\"path\":\"/\","
                + "\"parameters\":{\"parameter\":[]},\"headers\":{\"header\":[]}},"
                + "\"attributes\":{\"attrib\":[]}}]}}]}";
        DEFINITION.validate(storedJson);
        final HttpDatacollectionConfig fromJson = ConfigConvertUtil.jsonToObject(storedJson, HttpDatacollectionConfig.class);

        final String xml = JaxbUtils.marshal(fromJson);

        assertThat(xml, not(containsString("<parameters/>")));
        assertThat(xml, not(containsString("<headers/>")));
        assertThat(xml, not(containsString("<attributes/>")));
        assertThat(JaxbUtils.unmarshal(HttpDatacollectionConfig.class, xml, true), equalTo(fromJson));
    }

    @Test
    public void writtenJsonPassesSchemaValidation() throws IOException {
        final HttpDatacollectionConfig config = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, resource("http-datacollection-config.xml"));
        DEFINITION.validate(ConfigConvertUtil.objectToJson(config));
    }

    @Test
    public void storedJsonIsUnchanged() throws Exception {
        final HttpDatacollectionConfig config = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, resource("http-datacollection-config.xml"));
        final String stored = resource("http-datacollection-json/http-datacollection-config.json").trim();
        final String written = ConfigConvertUtil.objectToJson(config);

        JSONAssert.assertEquals(stored, written, true);
        assertThat("stored JSON reads back to the same object", ConfigConvertUtil.jsonToObject(stored, HttpDatacollectionConfig.class), equalTo(config));
    }

    /**
     * The schema allows type names (e.g. timeticks) that {@link AttributeType} doesn't know. Reading them
     * must fail like the XML adapter does instead of leaving the type null for the collector to trip over.
     */
    @Test
    public void unsupportedAttributeTypeIsRejected() {
        final String storedJson = jsonWithAttributeType("timeticks");
        DEFINITION.validate(storedJson);

        final ConfigConversionException e = assertThrows(ConfigConversionException.class,
                () -> ConfigConvertUtil.jsonToObject(storedJson, HttpDatacollectionConfig.class));

        final Throwable rootCause = rootCause(e);
        assertThat(rootCause, instanceOf(IllegalArgumentException.class));
        assertThat(rootCause.getMessage(), containsString("Unsupported attribute type 'timeticks'"));
    }

    @Test
    public void attributeTypeAliasesAreAccepted() {
        final HttpDatacollectionConfig config = ConfigConvertUtil.jsonToObject(jsonWithAttributeType("gauge32"), HttpDatacollectionConfig.class);
        assertThat(config.getHttpCollection().get(0).getUris().get(0).getAttributes().get(0).getType(), equalTo(AttributeType.GAUGE));
    }

    private static String jsonWithAttributeType(final String type) {
        return "{\"rrdRepository\":\"/rrd/\",\"http-collection\":[{\"name\":\"c\","
                + "\"rrd\":{\"step\":300,\"rra\":[\"RRA:AVERAGE:0.5:1:10\"]},"
                + "\"uris\":{\"uri\":[{\"name\":\"u\",\"url\":{\"path\":\"/\"},"
                + "\"attributes\":{\"attrib\":[{\"alias\":\"a\",\"match-group\":1,\"type\":\"" + type + "\"}]}}]}}]}";
    }

    private static Throwable rootCause(final Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static String resource(final String name) throws IOException {
        return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    }
}
