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

package org.opennms.core.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Documents parsed through JaxbUtils may carry a DOCTYPE with internal entities (imported
 * requisitions use them for named characters), but the number of entity expansions must be capped
 * so nested entities cannot exhaust memory. Runs against both the JDK parser and standalone Xerces,
 * which is the parser on the OpenNMS runtime classpath.
 */
@RunWith(Parameterized.class)
public class JaxbUtilsEntityExpansionTest {

    private static final String SAX_DRIVER = "org.xml.sax.driver";

    @XmlRootElement(name = "probe")
    public static class Probe {
        @XmlValue
        public String value;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> parsers() {
        return Arrays.asList(new Object[][] {
            { "com.sun.org.apache.xerces.internal.parsers.SAXParser" },
            { "org.apache.xerces.parsers.SAXParser" },
        });
    }

    private final String m_driver;
    private String m_previousDriver;

    public JaxbUtilsEntityExpansionTest(final String driver) {
        m_driver = driver;
    }

    @Before
    public void selectParser() {
        m_previousDriver = System.getProperty(SAX_DRIVER);
        System.setProperty(SAX_DRIVER, m_driver);
    }

    @After
    public void restoreParser() {
        if (m_previousDriver == null) {
            System.clearProperty(SAX_DRIVER);
        } else {
            System.setProperty(SAX_DRIVER, m_previousDriver);
        }
    }

    @Test
    public void internalEntitiesStillExpand() {
        final String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE probe [ <!ENTITY auml \"&#228;\"> ]>\n"
                + "<probe>M&auml;rz</probe>";
        assertEquals("März", JaxbUtils.unmarshal(Probe.class, xml).value);
    }

    @Test
    public void entityExpansionIsCapped() {
        // More expansions than either parser's limit (JDK 64,000, Xerces 100,000) of a one-character entity.
        final StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>\n<!DOCTYPE probe [ <!ENTITY m \"x\"> ]>\n<probe>");
        for (int i = 0; i < 150_000; i++) {
            xml.append("&m;");
        }
        xml.append("</probe>");
        try {
            JaxbUtils.unmarshal(Probe.class, xml.toString());
            fail("expected the entity expansion limit to reject the document");
        } catch (final RuntimeException e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            assertTrue("rejected for an unexpected reason: " + cause, String.valueOf(cause.getMessage()).contains("entity expansions"));
        }
    }
}
