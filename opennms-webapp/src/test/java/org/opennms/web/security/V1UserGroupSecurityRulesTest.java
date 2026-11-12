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
 * Checks the /rest rules of the deprecated v1 users and groups APIs in applicationContext-spring-security.xml.
 * <p>
 * Writing users, groups and the group authorizations of categories needs ROLE_ADMIN, the same as the v2 APIs.
 * The reads keep the rule of the other /rest reads.
 * <p>
 * The test reads the rules in file order and uses the first rule that matches,
 * the same as Spring Security.
 */
public class V1UserGroupSecurityRulesTest {

    private static final String SECURITY_XML = "src/main/webapp/WEB-INF/applicationContext-spring-security.xml";
    private static final String CHAIN = "/rest/**";
    private static final String ADMIN = "hasAnyRole('ROLE_ADMIN')";
    private static final String REST_WRITE = "hasAnyRole('ROLE_REST','ROLE_ADMIN')";
    private static final String REST_READ = "hasAnyRole('ROLE_REST','ROLE_ADMIN','ROLE_MOBILE','ROLE_USER')";
    private static final String[] WRITE_METHODS = { "POST", "PUT", "DELETE", "PATCH" };

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
    public void onlyAdminCanWriteUsersAndGroups() {
        final String[] paths = {
            "/rest/users",
            "/rest/users/",
            "/rest/users/jroe",
            "/rest/users/jroe/roles/ROLE_PROVISION",
            "/rest/users.xml",
            "/rest/users.json",
            "/rest/groups",
            "/rest/groups/",
            "/rest/groups/NOC",
            "/rest/groups/NOC/users/jroe",
            "/rest/groups/NOC/categories/Routers",
            "/rest/groups.xml",
            "/rest/groups.json",
            "/rest/categories/Routers/groups/NOC",
        };
        for (final String path : paths) {
            for (final String method : WRITE_METHODS) {
                assertEquals(method + " " + path, ADMIN, accessFor(path, method));
            }
        }
    }

    @Test
    public void readsAreNotChanged() {
        for (final String path : new String[] { "/rest/users", "/rest/users/jroe", "/rest/users/whoami", "/rest/groups",
                "/rest/groups/NOC/users", "/rest/groups/NOC/categories", "/rest/categories/groups/NOC",
                "/rest/categories/Routers/groups/NOC" }) {
            assertEquals("GET " + path, REST_READ, accessFor(path, "GET"));
            assertEquals("HEAD " + path, REST_READ, accessFor(path, "HEAD"));
        }
    }

    @Test
    public void otherCategoryWritesAreNotChanged() {
        assertEquals(REST_WRITE, accessFor("/rest/categories", "POST"));
        assertEquals(REST_WRITE, accessFor("/rest/categories/Routers", "PUT"));
        assertEquals(REST_WRITE, accessFor("/rest/categories/Routers", "DELETE"));
        assertEquals(REST_WRITE, accessFor("/rest/categories/Routers/nodes/1", "PUT"));
        assertEquals(REST_WRITE, accessFor("/rest/categories/Routers/nodes/1", "DELETE"));
        // a category named "groups" is still a category
        assertEquals(REST_WRITE, accessFor("/rest/categories/groups", "PUT"));
        // a root that only starts with "users" is not the users API
        assertEquals(REST_WRITE, accessFor("/rest/usersettings", "PUT"));
    }
}
