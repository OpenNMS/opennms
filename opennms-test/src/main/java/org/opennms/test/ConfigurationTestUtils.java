package org.opennms.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.Assert;

public class ConfigurationTestUtils extends Assert {
    public static Reader getReaderForResource(Object obj, String resource) {
        return new InputStreamReader(getInputStreamForResource(obj, resource));
    }

    public static InputStream getInputStreamForResource(Object obj,
            String resource) {
        InputStream is = obj.getClass().getResourceAsStream(resource);
        assertNotNull("could not get file resource '" + resource + "'", is);
        return is;
    }
    
    public static Reader getReaderForResourceWithReplacements(Object obj,
            String resource, String[][] replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new StringReader(newConfig);
    }
    
    
    public static InputStream getInputStreamForResourceWithReplacements(Object obj,
            String resource, String[][] replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new ByteArrayInputStream(newConfig.getBytes());
    }
    
    
    public static String getConfigForResourceWithReplacements(Object obj,
            String resource, String[][] replacements) throws IOException {

        Reader inputReader = getReaderForResource(obj, resource);
        BufferedReader bufferedReader = new BufferedReader(inputReader);
        
        StringBuffer buffer = new StringBuffer();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
    
        String newConfig = buffer.toString();
        for (String[] replacement : replacements) {
            newConfig = newConfig.replaceAll(replacement[0], replacement[1]);
        }
    
        return newConfig;
    }

}
