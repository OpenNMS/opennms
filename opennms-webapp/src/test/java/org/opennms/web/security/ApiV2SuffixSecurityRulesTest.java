/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks the /api/v2 rules in applicationContext-spring-security.xml.
 * <p>
 * The v2 CXF server maps the .xml, .json and .atom suffixes to the same resource.
 * Thus "/api/v2/snmp-config.json" is the snmp-config root. A "&lt;root&gt;/**" pattern does
 * not match this path, so each restricted root also needs a "&lt;root&gt;.*" rule.
 * <p>
 * The test reads the rules in file order and uses the first rule that matches,
 * the same as Spring Security.
 */
public class ApiV2SuffixSecurityRulesTest {

    private static final String SECURITY_XML = "src/main/webapp/WEB-INF/applicationContext-spring-security.xml";
    private static final String CHAIN = "/api/v2/**";
    private static final Pattern ROOT_RULE = Pattern.compile("^/api/v2/([A-Za-z0-9_-]+)/\\*\\*$");

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static List<Rule> rules;

    private static final class Rule {
        final String pattern;
        final String method;
        final String access;

        Rule(final String pattern, final String method, final String access) {
            this.pattern = pattern;
            this.method = method;
            this.access = access;
        }

        boolean matches(final String path, final String httpMethod) {
            return (method == null || method.equals(httpMethod)) && MATCHER.match(pattern, path);
        }
    }

    @BeforeClass
    public static void readRules() throws Exception {
        MATCHER.setTrimTokens(false);
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        final Document doc = dbf.newDocumentBuilder().parse(new File(SECURITY_XML));
        final NodeList chains = doc.getElementsByTagName("http");
        for (int i = 0; i < chains.getLength(); i++) {
            final Element chain = (Element) chains.item(i);
            if (!CHAIN.equals(chain.getAttribute("pattern"))) {
                continue;
            }
            rules = new ArrayList<>();
            final NodeList children = chain.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                final Node child = children.item(j);
                if (child instanceof Element && "intercept-url".equals(((Element) child).getTagName())) {
                    final Element rule = (Element) child;
                    final String method = rule.getAttribute("method");
                    rules.add(new Rule(rule.getAttribute("pattern"), method.isEmpty() ? null : method, rule.getAttribute("access")));
                }
            }
        }
        assertFalse("No rules found for the " + CHAIN + " chain", rules == null || rules.isEmpty());
    }

    private static String accessFor(final String path, final String method) {
        for (final Rule rule : rules) {
            if (rule.matches(path, method)) {
                return rule.access;
            }
        }
        return null;
    }

    @Test
    public void suffixedPathsUseTheRuleOfTheRoot() {
        final String[][] cases = {
            { "/api/v2/snmp-config", "GET" },
            { "/api/v2/trapd", "GET" },
            { "/api/v2/mibs", "GET" },
            { "/api/v2/mibs", "HEAD" },
            { "/api/v2/geocoding", "GET" },
            { "/api/v2/eventconf", "POST" },
            { "/api/v2/datacollectionconf", "PUT" },
            { "/api/v2/discovery", "POST" },
        };
        for (final String[] c : cases) {
            final String expected = accessFor(c[0], c[1]);
            for (final String suffix : new String[] { ".json", ".xml", ".atom" }) {
                assertEquals(c[1] + " " + c[0] + suffix, expected, accessFor(c[0] + suffix, c[1]));
            }
        }
    }

    @Test
    public void userCannotReadSnmpConfigWithASuffix() {
        assertEquals("hasAnyRole('ROLE_ADMIN','ROLE_PROVISION')", accessFor("/api/v2/snmp-config.json", "GET"));
        assertEquals("hasAnyRole('ROLE_ADMIN','ROLE_PROVISION')", accessFor("/api/v2/snmp-config.xml", "GET"));
    }

    /**
     * Each restricted "&lt;root&gt;/**" rule must have a "&lt;root&gt;.*" rule with the same method and access.
     * A rule that gives access to ROLE_USER is not a restricted rule, so the test ignores it.
     */
    @Test
    public void eachRestrictedRootHasASuffixRule() {
        int checked = 0;
        for (final Rule rule : rules) {
            final Matcher m = ROOT_RULE.matcher(rule.pattern);
            if (!m.matches() || rule.access.contains("ROLE_USER")) {
                continue;
            }
            final String suffixPattern = "/api/v2/" + m.group(1) + ".*";
            final boolean found = rules.stream().anyMatch(r -> r.pattern.equals(suffixPattern)
                    && (r.method == null ? rule.method == null : r.method.equals(rule.method))
                    && r.access.equals(rule.access));
            assertTrue("Missing rule: pattern=\"" + suffixPattern + "\" method=\"" + rule.method
                    + "\" access=\"" + rule.access + "\"", found);
            checked++;
        }
        assertTrue("No restricted /api/v2 root rules found", checked > 0);
    }
}
