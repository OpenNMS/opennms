/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 24, 2008  - rdk@krupczak.org
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/

/** 
 *
   OpenNMS Xmp config factory for kicking off parsing of the
   xmp-config config file for protocol specific options.
   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
   @version $Id: XmpConfigFactory.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;

public class XmpConfigFactory {

    /* class variables and methods *********************** */
    private static XmpConfigFactory instance;
    private static boolean loadedFromFile = false;
    private static boolean initialized = false;
    private static XmpConfig config = null;

    // initialize our class for the creation of instances
    public static void init() throws IOException, FileNotFoundException, MarshalException, ValidationException 
    {

        if (instance == null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.XMP_CONFIG_FILE_NAME);
            // create instance of ourselves and that causes
            // config file to be read and XmpConfig to be instantiated
            instance = new XmpConfigFactory(cfgFile.getPath());
            loadedFromFile = true;
            initialized = true;
        }
    }

    public static XmpConfig getXmpConfig() { return config; }

    public static XmpConfigFactory getInstance() { return instance; }

    /* instance variables ******************************** */

    /* constructors  ************************************* */

    public XmpConfigFactory(String configFile) 
    throws MarshalException, ValidationException, IOException 
    { 
        InputStream cfgIn = new FileInputStream(configFile);

        config = (XmpConfig)Unmarshaller.unmarshal(XmpConfig.class,
                                                   new InputStreamReader(cfgIn));
        cfgIn.close();
        return; 
    }

    public XmpConfigFactory(Reader rdr) 
    throws MarshalException, ValidationException, IOException 
    {
        config = (XmpConfig)Unmarshaller.unmarshal(XmpConfig.class,rdr);
    }

    /* private methods *********************************** */

    /* public methods ************************************ */

} /* class XmpConfigFactory */
