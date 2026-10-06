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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.stream.XMLInputFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * XmlHandler unmarshals XML that crosses the Minion-to-core IPC boundary, so a crafted message must
 * not be able to use a DTD or external entity (XXE) to read files, reach internal services or
 * expand entities. Runs against the JDK StAX parser and Woodstox, which is the StAX parser on the
 * OpenNMS runtime classpath.
 */
@RunWith(Parameterized.class)
public class XmlHandlerXxeTest {

    private static final String STAX_FACTORY = "javax.xml.stream.XMLInputFactory";

    private static final String SECRET = "SECRET-FROM-DISK";

    @XmlRootElement(name = "probe")
    public static class Probe {
        @XmlValue
        public String value;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> parsers() {
        return Arrays.asList(new Object[][] {
            { "com.sun.xml.internal.stream.XMLInputFactoryImpl" },
            { "com.ctc.wstx.stax.WstxInputFactory" },
        });
    }

    @Rule
    public TemporaryFolder m_folder = new TemporaryFolder();

    private final String m_factory;
    private String m_previousFactory;

    public XmlHandlerXxeTest(final String factory) {
        m_factory = factory;
    }

    @Before
    public void selectParser() {
        m_previousFactory = System.getProperty(STAX_FACTORY);
        System.setProperty(STAX_FACTORY, m_factory);
    }

    @After
    public void restoreParser() {
        if (m_previousFactory == null) {
            System.clearProperty(STAX_FACTORY);
        } else {
            System.setProperty(STAX_FACTORY, m_previousFactory);
        }
    }

    @Test
    public void unmarshalsAnOrdinaryMessage() {
        assertEquals(m_factory, XMLInputFactory.newInstance().getClass().getName());
        assertEquals("hello", new XmlHandler<>(Probe.class).unmarshal("<probe>hello</probe>").value);
    }

    @Test
    public void doesNotReadAnExternalEntity() throws Exception {
        final File secret = m_folder.newFile("secret.txt");
        Files.write(secret.toPath(), SECRET.getBytes(StandardCharsets.UTF_8));
        assertNotLeaked("<!DOCTYPE probe [ <!ENTITY xxe SYSTEM \"" + secret.toURI() + "\"> ]>\n"
                + "<probe>&xxe;</probe>");
    }

    @Test
    public void doesNotLoadAnExternalParameterEntity() throws Exception {
        final File dtd = m_folder.newFile("external.dtd");
        Files.write(dtd.toPath(), ("<!ENTITY leak \"" + SECRET + "\">").getBytes(StandardCharsets.UTF_8));
        assertNotLeaked("<!DOCTYPE probe [ <!ENTITY % ext SYSTEM \"" + dtd.toURI() + "\"> %ext; ]>\n"
                + "<probe>&leak;</probe>");
    }

    @Test
    public void doesNotExpandInternalEntities() {
        // Nested internal entities ("billion laughs") must not expand.
        assertNotLeaked("<!DOCTYPE probe [ <!ENTITY a \"" + SECRET + "\"> <!ENTITY b \"&a;&a;\"> ]>\n"
                + "<probe>&b;</probe>");
    }

    private void assertNotLeaked(final String xml) {
        String value;
        try {
            value = new XmlHandler<>(Probe.class).unmarshal(xml).value;
        } catch (final RuntimeException e) {
            value = null;
        }
        assertFalse("the parser expanded an entity: " + value, value != null && value.contains(SECRET));
    }
}
