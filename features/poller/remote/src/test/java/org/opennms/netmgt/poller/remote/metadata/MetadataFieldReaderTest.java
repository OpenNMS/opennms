/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.poller.remote.PollerTheme;

public class MetadataFieldReaderTest {
    @Test
    public void testValidFields() throws Exception {
        final Set<MetadataField> fields = getReader("target/test-classes/metadata.properties").getMetadataFields();
        assertEquals(3, fields.size());
        final Iterator<MetadataField> i = fields.iterator();

        assertTrue(i.hasNext());
        MetadataField field = i.next();
        assertEquals("Foo", field.getDescription());
        assertNotNull(field.getValidator());
        assertTrue(field.getValidator().getClass().equals(IntegerValidator.class));
        assertTrue(field.isRequired());
        
        assertTrue(i.hasNext());
        field = i.next();
        assertEquals("Bar", field.getDescription());
        assertNotNull(field.getValidator());
        assertTrue(field.getValidator().getClass().equals(InetAddressValidator.class));
        assertFalse(field.isRequired());

        assertTrue(i.hasNext());
        field = i.next();
        assertEquals("Baz", field.getDescription());
        assertNull(field.getValidator());
        assertTrue(field.isRequired());

        assertFalse(i.hasNext());
    }

    @Test(expected=ClassNotFoundException.class)
    public void testInvalidValidator() throws Exception {
        getReader("target/test-classes/invalid-validator.properties").getMetadataFields();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMissingDescription() throws Exception {
        getReader("target/test-classes/missing-description.properties").getMetadataFields();
    }

    @Test
    public void testValidTheme() throws Exception {
        final PollerTheme theme = getReader("target/test-classes/metadata.properties").getTheme();
        assertEquals("Foo", theme.getTitle());
        assertEquals(new URL("http://blah/"), theme.getImage());
        assertEquals(new Color(0xffffff), theme.getForegroundColor());
        assertEquals(new Color(0x000000), theme.getBackgroundColor());
        assertEquals(new Color(0xff0000), theme.getDetailColor());
    }

    private MetadataFieldReader getReader(final String propFile) throws URISyntaxException {
        final File propertiesFile = new File(propFile);
        assertTrue(propertiesFile.exists());
        return new MetadataFieldReader(propertiesFile);
    }
}
