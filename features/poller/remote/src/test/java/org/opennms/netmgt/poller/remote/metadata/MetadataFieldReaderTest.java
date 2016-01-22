package org.opennms.netmgt.poller.remote.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

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

    private MetadataFieldReader getReader(final String propFile) throws URISyntaxException {
        final File propertiesFile = new File(propFile);
        assertTrue(propertiesFile.exists());
        return new MetadataFieldReader(propertiesFile);
    }
}
