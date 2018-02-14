/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
 *   OpenNMS Xmp config factory for kicking off parsing of the
 *   xmp-config config file for protocol specific options.
 *   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
 *   @version $Id: XmpConfigFactory.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.protocols.xmp.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;

public class XmpConfigFactory {

    /* class variables and methods *********************** */
    private static XmpConfigFactory instance;
    private XmpConfig config = null;

    // initialize our class for the creation of instances
    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static void init() throws IOException, FileNotFoundException 
    {

        if (instance == null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.XMP_CONFIG_FILE_NAME);
            // create instance of ourselves and that causes
            // config file to be read and XmpConfig to be instantiated
            instance = new XmpConfigFactory(cfgFile.getPath());
        }
    }

    /**
     * <p>getXmpConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.xmpConfig.XmpConfig} object.
     */
    public XmpConfig getXmpConfig() { return config; }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory} object.
     */
    public static XmpConfigFactory getInstance() { return instance; }

    public static void setInstance(XmpConfigFactory instance) {
        XmpConfigFactory.instance = instance;
    }

    /* instance variables ******************************** */

    /* constructors  ************************************* */

    /**
     * <p>Constructor for XmpConfigFactory.</p>
     *
     * @param configFile a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public XmpConfigFactory(String configFile) 
    throws IOException 
    {
        config = JaxbUtils.unmarshal(XmpConfig.class, configFile);
        return; 
    }

    /**
     * <p>Constructor for XmpConfigFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws java.io.IOException if any.
     */
    public XmpConfigFactory(Reader rdr) 
    throws IOException 
    {
        config = (XmpConfig)JaxbUtils.unmarshal(XmpConfig.class,rdr);
    }

    /* private methods *********************************** */

    /* public methods ************************************ */

} /* class XmpConfigFactory */
