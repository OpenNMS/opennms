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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks the access values of every expression chain in applicationContext-spring-security.xml.
 * <p>
 * A chain with use-expressions="true" (the default since Spring Security 4) evaluates each access value as SpEL.
 * A bare authority such as "ROLE_ADMIN" is then read as a property and throws for every user, and a list such as
 * "ROLE_ADMIN,ROLE_REST" does not even parse. Both must be written as hasAnyRole(...).
 */
public class ExpressionChainAccessRulesTest {

    private static final String SECURITY_XML = "src/main/webapp/WEB-INF/applicationContext-spring-security.xml";
    private static final Pattern BARE_AUTHORITY = Pattern.compile("^ROLE_\\w+$");

    static boolean isBareAuthorityList(final String access) {
        for (final String token : access.split(",")) {
            if (BARE_AUTHORITY.matcher(token.trim()).matches()) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void recognizesBareAuthorities() {
        assertTrue(isBareAuthorityList("ROLE_ADMIN"));
        assertTrue(isBareAuthorityList("ROLE_ADMIN,ROLE_REST"));
        assertTrue(isBareAuthorityList("ROLE_ADMIN, ROLE_REST"));
        assertFalse(isBareAuthorityList("hasAnyRole('ROLE_ADMIN','ROLE_REST')"));
        assertFalse(isBareAuthorityList("hasRole('ROLE_ADMIN')"));
        assertFalse(isBareAuthorityList("permitAll"));
        assertFalse(isBareAuthorityList("isAuthenticated()"));
    }

    @Test
    public void eachRuleOfAnExpressionChainUsesAnExpression() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        final Document doc = dbf.newDocumentBuilder().parse(new File(SECURITY_XML));

        final List<String> violations = new ArrayList<>();
        int chainsChecked = 0;
        int rulesChecked = 0;

        final NodeList chains = doc.getElementsByTagName("http");
        for (int i = 0; i < chains.getLength(); i++) {
            final Element chain = (Element) chains.item(i);
            if ("false".equals(chain.getAttribute("use-expressions"))) {
                continue;
            }
            final String chainName = chain.hasAttribute("pattern") ? chain.getAttribute("pattern")
                    : "request-matcher-ref=" + chain.getAttribute("request-matcher-ref");
            chainsChecked++;

            final NodeList children = chain.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                final Node child = children.item(j);
                if (!(child instanceof Element) || !"intercept-url".equals(((Element) child).getTagName())) {
                    continue;
                }
                final Element rule = (Element) child;
                final String access = rule.getAttribute("access");
                rulesChecked++;
                if (isBareAuthorityList(access)) {
                    violations.add("chain=\"" + chainName + "\" pattern=\"" + rule.getAttribute("pattern")
                            + "\" method=\"" + rule.getAttribute("method") + "\" access=\"" + access + "\"");
                }
            }
        }

        assertTrue("Expected several expression chains, found " + chainsChecked, chainsChecked > 1);
        assertTrue("No intercept-url rules found in the expression chains", rulesChecked > 0);
        assertTrue("Bare authorities in expression chains:\n  " + String.join("\n  ", violations), violations.isEmpty());
    }
}
