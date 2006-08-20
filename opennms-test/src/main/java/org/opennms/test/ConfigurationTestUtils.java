package org.opennms.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import junit.framework.Assert;

public class ConfigurationTestUtils extends Assert {
    public static Reader getReaderForResource(Object obj, String resource) {
        InputStream is = obj.getClass().getResourceAsStream(resource);
        assertNotNull("could not get file resource '" + resource + "'", is);
        return new InputStreamReader(is);
    }
}
