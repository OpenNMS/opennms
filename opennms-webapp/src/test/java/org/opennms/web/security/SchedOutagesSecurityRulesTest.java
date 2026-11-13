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
package org.opennms.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks the /rest/sched-outages rules in applicationContext-spring-security.xml.
 * <p>
 * The applies-to endpoints list poller, collectd and threshd package names, so only
 * ROLE_REST and ROLE_ADMIN can read them (NMS-20268). The applies-to summary holds
 * no package names, so every role that reads the outages can read it (NMS-20579).
 * <p>
 * The test reads the rules in file order and uses the first rule that matches,
 * the same as Spring Security. Paths are decoded, as Spring Security matches them.
 */
public class SchedOutagesSecurityRulesTest {

    private static final String SECURITY_XML = "src/main/webapp/WEB-INF/applicationContext-spring-security.xml";
    private static final String CHAIN = "/rest/**";
    private static final String READ = "hasAnyRole('ROLE_REST','ROLE_ADMIN','ROLE_MOBILE','ROLE_USER')";
    private static final String REST_OR_ADMIN = "hasAnyRole('ROLE_REST','ROLE_ADMIN')";
    private static final String[] SUFFIXES = { "", ".json", ".xml", ".atom" };
    private static final String[] SUMMARY_SUFFIXES = { "", ".json", ".xml", ".atom", "/" };

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static List<String[]> rules;

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
                    rules.add(new String[] { rule.getAttribute("pattern"), rule.getAttribute("method"), rule.getAttribute("access") });
                }
            }
        }
        assertFalse("No rules found for the " + CHAIN + " chain", rules == null || rules.isEmpty());
    }

    /** Returns the access expression of the first rule that matches, or null. */
    private static String accessFor(final String path, final String method) {
        for (final String[] rule : rules) {
            if ((rule[1].isEmpty() || rule[1].equals(method)) && MATCHER.match(rule[0], path)) {
                return rule[2];
            }
        }
        return null;
    }

    @Test
    public void everyReaderCanReadTheSummary() {
        for (final String path : new String[] {
                "/rest/sched-outages/applies-to/summary",
                "/rest/sched-outages/Weekend maintenance/applies-to/summary" }) {
            for (final String suffix : SUMMARY_SUFFIXES) {
                assertEquals("GET " + path + suffix, READ, accessFor(path + suffix, "GET"));
            }
        }
    }

    @Test
    public void onlyRestAndAdminCanReadPackageNames() {
        for (final String path : new String[] {
                "/rest/sched-outages/applies-to",
                "/rest/sched-outages/applies-to/",
                "/rest/sched-outages/Weekend maintenance/applies-to",
                "/rest/sched-outages/Weekend maintenance/applies-to/" }) {
            assertEquals("GET " + path, REST_OR_ADMIN, accessFor(path, "GET"));
        }
    }

    /** The v1 API serves "applies-to.json" as "applies-to", so the suffixed forms need the same rule. */
    @Test
    public void onlyRestAndAdminCanReadPackageNamesWithASuffix() {
        for (final String path : new String[] {
                "/rest/sched-outages/applies-to",
                "/rest/sched-outages/Weekend maintenance/applies-to" }) {
            for (final String suffix : SUFFIXES) {
                assertEquals("GET " + path + suffix, REST_OR_ADMIN, accessFor(path + suffix, "GET"));
            }
        }
    }

    @Test
    public void summaryDoesNotOpenWrites() {
        for (final String method : new String[] { "POST", "PUT", "DELETE" }) {
            assertEquals(method, REST_OR_ADMIN, accessFor("/rest/sched-outages/Weekend maintenance/applies-to/summary", method));
        }
    }

    @Test
    public void otherReadsAreNotChanged() {
        assertEquals(READ, accessFor("/rest/sched-outages", "GET"));
        assertEquals(READ, accessFor("/rest/sched-outages/Weekend maintenance", "GET"));
        assertEquals(READ, accessFor("/rest/sched-outages/nodeInOutage/1", "GET"));
    }
}
