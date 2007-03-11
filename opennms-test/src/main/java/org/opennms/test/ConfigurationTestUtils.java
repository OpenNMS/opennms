//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        assertNotNull("could not get resource '" + resource + "' as an input stream", is);
        return is;
    }
    
    public static Reader getReaderForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new StringReader(newConfig);
    }
    
    
    public static InputStream getInputStreamForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {
        String newConfig = getConfigForResourceWithReplacements(obj, resource,
                                                                replacements);
        return new ByteArrayInputStream(newConfig.getBytes());
    }
    
    
    public static String getConfigForResourceWithReplacements(Object obj,
            String resource, String[] ... replacements) throws IOException {

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

    public static Reader getReaderForConfigFile(String configFile) throws FileNotFoundException {
        return new InputStreamReader(getInputStreamForConfigFile(configFile));
    }

    public static InputStream getInputStreamForConfigFile(String configFile) throws FileNotFoundException {
        String path = "../opennms-daemon/src/main/filtered/etc/" + configFile;
        File file = new File(path);
        assertTrue("configuration file '" + configFile + "' does not exist at " + path, file.exists());
        InputStream is = new FileInputStream(file);
        return is;
    }

}
