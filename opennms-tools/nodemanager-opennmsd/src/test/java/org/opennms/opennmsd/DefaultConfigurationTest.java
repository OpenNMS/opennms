/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.opennmsd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

public class DefaultConfigurationTest extends TestCase {
    
    public void testFileDoesntExist() {
        DefaultConfiguration config = new DefaultConfiguration();
        config.setConfigFile(new File("doesntexist"));
        try {
            config.load();
            fail("should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
            
        } catch(Throwable t) {
            fail("did not expect exception "+t);
        }
        
    }
    
    public void testLoadFile() throws IOException {
        File confFile = getTestConfFile("loadTest-opennmsd.conf");
        
        DefaultConfiguration config = new DefaultConfiguration();
        config.setConfigFile(confFile);
        config.load();
        
	//        assertNotNull(config.getFilterChain());
    }
    

    private File getTestConfFile(String fileName) {
        File resourcesDir = new File(new File(new File("src"), "test"), "resources");
        return new File(resourcesDir, fileName);
    }
    
}
