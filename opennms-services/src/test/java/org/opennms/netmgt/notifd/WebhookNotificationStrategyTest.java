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
package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.model.notifd.Argument;

public class WebhookNotificationStrategyTest {

    private final WebhookNotificationStrategy m_strategy = new WebhookNotificationStrategy();

    private static Argument arg(final String argSwitch, final String value) {
        return new Argument(argSwitch, null, value, false);
    }

    private static Argument substituted(final String argSwitch, final String substitution) {
        return new Argument(argSwitch, substitution, "", false);
    }

    private void withArguments(final Argument... arguments) {
        m_strategy.setArguments(new ArrayList<>(Arrays.asList(arguments)));
    }

    @After
    public void clearProperties() {
        System.clearProperty("org.opennms.netmgt.notifd.webhook.url");
        System.clearProperty("org.opennms.netmgt.notifd.webhook.teams.url");
    }

    @Test
    public void testTemplateSubstitution() {
        withArguments(arg("-subject", "Node down"), arg("-tm", "webServer1 is down"));

        assertEquals("{\"text\": \"Node down: webServer1 is down\"}",
                m_strategy.renderTemplate("{\"text\": \"${subject}: ${textMessage}\"}", true));
    }

    @Test
    public void testJsonMetacharactersAreEscaped() {
        withArguments(arg("-subject", "He said \"down\""), arg("-tm", "line one\nline two\\end"));

        final String rendered = m_strategy.renderTemplate("{\"text\": \"${subject}|${textMessage}\"}", true);

        assertEquals("{\"text\": \"He said \\\"down\\\"|line one\\nline two\\\\end\"}", rendered);
        assertTrue("escaped payload must still parse", WebhookNotificationStrategy.isWellFormedJson(rendered));
    }

    @Test
    public void testRawModifierSkipsEscaping() {
        withArguments(arg("-embed", "{\"title\": \"raw\"}"));

        assertEquals("{\"embeds\": {\"title\": \"raw\"}}",
                m_strategy.renderTemplate("{\"embeds\": ${embed|raw}}", true));
    }

    @Test
    public void testNoEscapingForNonJsonContent() {
        withArguments(arg("-subject", "He said \"down\""));

        assertEquals("subject=He said \"down\"", m_strategy.renderTemplate("subject=${subject}", false));
    }

    @Test
    public void testMetadataTokensAreLeftAlone() {
        // A colon-bearing token belongs to the metadata DSL, which runs before the
        // strategy. Anything still present here must survive untouched.
        withArguments(arg("-subject", "ignored"));

        assertEquals("${scv:webhook:token}", m_strategy.renderTemplate("${scv:webhook:token}", true));
    }

    @Test
    public void testUndeclaredTokenRendersEmpty() {
        withArguments(arg("-subject", "Node down"));

        assertEquals("{\"text\": \"\"}", m_strategy.renderTemplate("{\"text\": \"${nosuchthing}\"}", true));
    }

    @Test
    public void testUndashedParameterNamesResolve() {
        // noticeid, eventID and eventUEI reach notifd without a leading dash.
        withArguments(arg("noticeid", "42"));

        assertEquals("42", m_strategy.renderTemplate("${noticeid}", false));
    }

    @Test
    public void testValueBeatsSubstitutionBeatsSystemProperty() {
        System.setProperty("org.opennms.netmgt.notifd.webhook.url", "http://from-property/");

        withArguments(new Argument("-url", "http://from-substitution/", "http://from-value/", false));
        assertEquals("http://from-value/", m_strategy.getValue("-url"));

        withArguments(substituted("-url", "http://from-substitution/"));
        assertEquals("http://from-substitution/", m_strategy.getValue("-url"));

        withArguments(arg("-url", ""));
        assertEquals("http://from-property/", m_strategy.getValue("-url"));
    }

    @Test
    public void testNamedInstancePropertyWins() {
        System.setProperty("org.opennms.netmgt.notifd.webhook.url", "http://generic/");
        System.setProperty("org.opennms.netmgt.notifd.webhook.teams.url", "http://teams/");

        withArguments(arg("-name", "teams"), arg("-url", ""));
        assertEquals("http://teams/", m_strategy.getValue("-url"));

        withArguments(arg("-name", "discord"), arg("-url", ""));
        assertEquals("http://generic/", m_strategy.getValue("-url"));
    }

    @Test
    public void testHeadersAreCollected() {
        withArguments(arg("-header-Authorization", "Bearer sekrit"),
                arg("-header-X-Custom", "yes"),
                arg("-header-", "no name"),
                arg("-url", "http://example.org/"));

        final Map<String, String> headers = m_strategy.getHeaders();

        assertEquals(2, headers.size());
        assertEquals("Bearer sekrit", headers.get("Authorization"));
        assertEquals("yes", headers.get("X-Custom"));
    }

    @Test
    public void testSuccessIsAnyTwoHundred() {
        withArguments(arg("-url", "http://example.org/"));

        // Discord answers 204 with no body; the old Slack code called that a failure.
        assertTrue(m_strategy.isSuccess(204, ""));
        assertTrue(m_strategy.isSuccess(200, "ok"));
        assertTrue(m_strategy.isSuccess(201, "1"));
        assertFalse(m_strategy.isSuccess(500, "boom"));
        assertFalse(m_strategy.isSuccess(302, ""));
    }

    @Test
    public void testSuccessMatchNarrowsATwoHundred() {
        withArguments(arg("-success-match", "\"ok\"\\s*:\\s*true"));

        assertTrue(m_strategy.isSuccess(200, "{\"ok\": true}"));
        assertFalse(m_strategy.isSuccess(200, "{\"ok\": false, \"error\": \"channel_not_found\"}"));
        // A match in the body cannot rescue a non-2xx status.
        assertFalse(m_strategy.isSuccess(500, "{\"ok\": true}"));
    }

    @Test
    public void testInvalidSuccessMatchFailsClosed() {
        withArguments(arg("-success-match", "[unclosed"));

        assertFalse(m_strategy.isSuccess(200, "anything"));
    }

    @Test
    public void testMalformedJsonIsNotSent() {
        // No network involved: validation happens before the request is built.
        final List<Argument> arguments = Arrays.asList(
                arg("-url", "http://127.0.0.1:1/never-reached"),
                substituted("-body", "{\"text\": ${subject}}"),
                arg("-subject", "unquoted"));

        assertEquals(1, m_strategy.send(arguments));
    }

    @Test
    public void testMissingUrlFails() {
        assertEquals(1, m_strategy.send(new ArrayList<>()));
    }

    @Test
    public void testEscapeJsonHandlesControlCharacters() {
        assertEquals("a\\u0000b", WebhookNotificationStrategy.escapeJson("a" + (char) 0 + "b"));
        assertEquals("tab\\there", WebhookNotificationStrategy.escapeJson("tab\there"));
    }

    @Test
    public void testWellFormedJsonDetection() {
        assertTrue(WebhookNotificationStrategy.isWellFormedJson("{\"a\": 1}"));
        assertTrue(WebhookNotificationStrategy.isWellFormedJson("[1, 2]"));
        assertFalse(WebhookNotificationStrategy.isWellFormedJson("{\"a\": }"));
        assertFalse(WebhookNotificationStrategy.isWellFormedJson("{\"a\": \"un\"terminated\"}"));
    }

    @Test
    public void testWellFormedJsonRejectsBlankAndTrailingContent() {
        // Jackson maps all of these to a MissingNode or stops at the first value,
        // so they have to be rejected explicitly.
        assertFalse(WebhookNotificationStrategy.isWellFormedJson(null));
        assertFalse(WebhookNotificationStrategy.isWellFormedJson(""));
        assertFalse(WebhookNotificationStrategy.isWellFormedJson("   "));
        assertFalse(WebhookNotificationStrategy.isWellFormedJson("{\"a\": 1} then junk"));
        assertFalse(WebhookNotificationStrategy.isWellFormedJson("{\"a\": 1} {\"b\": 2}"));
    }

    @Test
    public void testRedactUrlKeepsOnlySchemeAndHost() {
        // Slack, Discord and Teams URLs carry the credential in the path or query.
        assertEquals("https://hooks.slack.com",
                WebhookNotificationStrategy.redactUrl("https://hooks.slack.com/services/T000/B000/sekrittoken"));
        assertEquals("https://example.org:8443",
                WebhookNotificationStrategy.redactUrl("https://example.org:8443/hook?token=sekrit"));
        // Credentials in the authority must not survive either.
        assertEquals("https://example.org",
                WebhookNotificationStrategy.redactUrl("https://user:pass@example.org/hook"));
        assertEquals("(unparseable URL)", WebhookNotificationStrategy.redactUrl("not a url"));
    }

    @Test
    public void testUnknownModifierIsIgnoredButStillSubstitutes() {
        withArguments(arg("-subject", "He said \"down\""));

        // The value is still escaped: an unknown modifier must not act like |raw.
        assertEquals("{\"text\": \"He said \\\"down\\\"\"}",
                m_strategy.renderTemplate("{\"text\": \"${subject|bogus}\"}", true));
    }

    @Test
    public void testNegativeTimeoutFallsBackToDefault() {
        withArguments(arg("-url", "http://example.org/"), arg("-connect-timeout", "-5"));

        // Exercised through send(): a negative timeout must not reach the client.
        assertEquals(1, m_strategy.send(Arrays.asList(
                arg("-url", "http://127.0.0.1:1/refused"),
                arg("-connect-timeout", "-5"),
                substituted("-body", "{\"text\": \"hi\"}"))));
    }

    @Test
    public void testNullArgumentsAreTolerated() {
        m_strategy.setArguments(null);

        assertNull(m_strategy.getValue("-url"));
        assertTrue(m_strategy.getHeaders().isEmpty());
    }

    private List<Argument> currentArguments() {
        return Arrays.asList(
                new Argument("-url", null, "http://127.0.0.1:1/never-reached", false),
                new Argument("-body", "{\"text\": ${subject}}", "", false),
                new Argument("-subject", null, "unquoted", false));
    }
}
