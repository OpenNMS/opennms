//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 13: Create log() method. - dj@opennms.org
// 2004 Aug 23: Created this file.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8
//
package org.opennms.netmgt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * Provides access to the default javamail configuration data.
 *
 * @author ranger
 * @version $Id: $
 */
public class JavaMailerConfig {
    
    /**
     * This loads the configuration file.
     *
     * @return a Properties object representing the configuration properties
     * @throws java.io.IOException if any.
     */
    public static synchronized Properties getProperties() throws IOException {
        log().debug("Loading javamail properties.");
        Properties properties = new Properties();
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.JAVA_MAIL_CONFIG_FILE_NAME);
        InputStream in = new FileInputStream(configFile);
        properties.load(in);
        in.close();
        return properties;
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(JavaMailerConfig.class);
    }

}
