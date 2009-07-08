/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.asterisk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;

/**
 * Provides access to the default Asterisk configuration data.
 */
public class AsteriskConfig {

    
    /**
     * This loads the configuration file.
     * 
     * @return a Properties object representing the configuration properties
     * @throws IOException
     */
    public static synchronized Properties getProperties() throws IOException {
        log().debug("Loading Asterisk configuration properties.");
        Properties properties = new Properties();
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.ASTERISK_CONFIG_FILE_NAME);
        InputStream in = new FileInputStream(configFile);
        properties.load(in);
        in.close();
        return properties;
    }
    
    private static Category log() {
        return ThreadCategory.getInstance(AsteriskConfig.class);
    }

}