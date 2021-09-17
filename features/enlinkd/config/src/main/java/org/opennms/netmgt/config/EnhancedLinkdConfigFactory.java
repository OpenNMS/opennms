/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * enhanced linkd service from the enlinkd-configuration xml file.
 *
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class EnhancedLinkdConfigFactory extends EnhancedLinkdConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(EnhancedLinkdConfigFactory.class);
    private static final String CONFIG_NAME = "enlinkd";
    private static final String DEFAULT_CONFIG_ID = "default";
    
    public EnhancedLinkdConfigFactory() throws IOException {
        reload();
    }

    /**
     * <p>Constructor for LinkdConfigFactory.</p>
     *
   //  * @param currentVersion a long.
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */


    /** {@inheritDoc} */


    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     */


    /**
     * <p>reloadXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */


    /**
     * Saves the current in-memory configuration to disk
     *
     * @throws java.io.IOException if any.
     */


    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return DEFAULT_CONFIG_ID;
    }


    @Override
    public void reload() throws IOException {
        this.loadConfig(this.getDefaultConfigId());
    }
}
