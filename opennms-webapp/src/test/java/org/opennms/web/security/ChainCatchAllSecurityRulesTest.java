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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks that each chain in applicationContext-spring-security.xml with method-specific rules fails closed.
 * <p>
 * The method rules use an exact-case comparison. Thus a request with a lowercase HTTP method matches
 * no method rule. The last rule of the chain must match all methods and all paths of the chain.
 */
public class ChainCatchAllSecurityRulesTest {

    private static final String SECURITY_XML = "src/main/webapp/WEB-INF/applicationContext-spring-security.xml";

    @Test
    public void eachChainWithMethodRulesEndsWithACatchAllRule() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        final Document doc = dbf.newDocumentBuilder().parse(new File(SECURITY_XML));
        final NodeList chains = doc.getElementsByTagName("http");
        int checked = 0;
        for (int i = 0; i < chains.getLength(); i++) {
            final Element chain = (Element) chains.item(i);
            final String chainName = chain.hasAttribute("pattern") ? chain.getAttribute("pattern") : chain.getAttribute("request-matcher-ref");
            boolean hasMethodRule = false;
            Element last = null;
            final NodeList children = chain.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                final Node child = children.item(j);
                if (child instanceof Element && "intercept-url".equals(((Element) child).getTagName())) {
                    last = (Element) child;
                    hasMethodRule |= last.hasAttribute("method");
                }
            }
            if (!hasMethodRule) {
                continue;
            }
            final String pattern = last.getAttribute("pattern");
            if (last.hasAttribute("method") || !("/**".equals(pattern) || pattern.equals(chain.getAttribute("pattern")))) {
                fail("The last rule of the " + chainName + " chain must match all methods and paths, for example"
                        + " <intercept-url pattern=\"/**\" access=\"denyAll\"/>");
            }
            checked++;
        }
        assertTrue("No chains with method rules found", checked > 0);
    }
}
