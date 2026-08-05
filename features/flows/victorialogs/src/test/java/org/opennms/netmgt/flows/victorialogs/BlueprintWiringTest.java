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

package org.opennms.netmgt.flows.victorialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The safety properties that live in the blueprint rather than in any class.
 *
 * <p>{@code VictoriaLogsQueryServiceRegistrarTest} constrains the registrar, which is not the same
 * thing as constraining the wiring. Two edits to XML would restore the hole the registrar exists to
 * close, with every one of those tests still green: re-adding a declarative {@code <service>} element
 * for {@code FlowQueryService}, or flipping a default to enable the backend on installs that never
 * asked for it. Neither is reachable from Java, so they are asserted here.
 *
 * <p>This does not start a container and proves nothing about how Aries behaves — it pins the
 * document, which is the part that can be changed by accident.
 */
public class BlueprintWiringTest {

    private static final String RESOURCE = "/OSGI-INF/blueprint/blueprint.xml";
    private static final String QUERY_SERVICE = "org.opennms.netmgt.flows.api.FlowQueryService";

    private static Document blueprint;
    private static String raw;

    @BeforeClass
    public static void load() throws Exception {
        try (final InputStream in = BlueprintWiringTest.class.getResourceAsStream(RESOURCE)) {
            assertNotNull("missing " + RESOURCE + " on the test classpath", in);
            raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (final InputStream in = BlueprintWiringTest.class.getResourceAsStream(RESOURCE)) {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            blueprint = factory.newDocumentBuilder().parse(in);
        }
    }

    /**
     * The query service must never be registered declaratively.
     *
     * <p>A {@code <service>} element is unconditional, and this interface is consumed through a
     * singleton reference: a registration that exists can be bound, whatever its ranking, the first
     * time the Elasticsearch container reloads and its own registration departs. Registration has to
     * stay behind {@code VictoriaLogsQueryServiceRegistrar}, which can decline.
     */
    @Test
    public void theQueryServiceIsNotRegisteredDeclaratively() {
        final NodeList services = blueprint.getElementsByTagNameNS("*", "service");
        for (int i = 0; i < services.getLength(); i++) {
            final Element service = (Element) services.item(i);
            assertFalse("blueprint.xml registers " + QUERY_SERVICE + " with a <service> element; "
                            + "it must go through VictoriaLogsQueryServiceRegistrar so that it can "
                            + "be withheld when disabled or misconfigured",
                    QUERY_SERVICE.equals(service.getAttribute("interface")));
        }
    }

    /** And it must be registered through the registrar, or nothing registers it at all. */
    @Test
    public void theRegistrarIsWiredWithTheClientItHasToConsult() {
        assertTrue("the registrar bean is missing; nothing would publish the query service",
                raw.contains("VictoriaLogsQueryServiceRegistrar"));
        assertTrue("the registrar must receive the client, or it cannot refuse to publish a "
                        + "misconfigured one", raw.contains("<argument ref=\"victoriaLogsClient\"/>"));
    }

    /**
     * Both opt-ins default to off.
     *
     * <p>The Java fields default to disabled too, but that is dead in production: blueprint always
     * injects the property, so these defaults are the only real guard. An install that has not been
     * pointed at a real VictoriaLogs must neither write to localhost:9428 nor answer the flow UI.
     */
    @Test
    public void bothOptInsDefaultToDisabled() {
        for (final String flag : new String[]{"skipVictoriaLogsPersistence", "skipVictoriaLogsQueries"}) {
            assertEquals(flag + " must default to true", "true", defaultOf(flag));
        }
    }

    /** The health check has to see both flags, or query-only mode reports "Not configured". */
    @Test
    public void theHealthCheckIsWiredToBothFlags() {
        assertTrue("the health check must receive the query flag as well as the persistence one",
                raw.contains("<property name=\"queriesDisabled\" value=\"${skipVictoriaLogsQueries}\"/>"));
    }

    private static String defaultOf(final String name) {
        final NodeList properties = blueprint.getElementsByTagNameNS("*", "property");
        for (int i = 0; i < properties.getLength(); i++) {
            final Element property = (Element) properties.item(i);
            if (name.equals(property.getAttribute("name")) && property.hasAttribute("value")
                    && property.getParentNode() != null
                    && "default-properties".equals(property.getParentNode().getLocalName())) {
                return property.getAttribute("value");
            }
        }
        throw new AssertionError("no cm:default-properties entry for " + name);
    }
}
