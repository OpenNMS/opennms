//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 14: Create a logStatic method and use setInstance to save the instance in init() - dj@opennms.org
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Jan 31: Added code to allow for RRA definitions within linkd packages.
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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * linkd service from the linkd-configuration xml file.
 *
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class LinkdConfigFactory extends LinkdConfigManager {
    /**
     * The singleton instance of this factory
     */
    private static LinkdConfig m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    /**
     * Private constructor
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @param currentVersion a long.
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    @Deprecated
    public LinkdConfigFactory(final long currentVersion, final Reader reader) throws MarshalException, ValidationException, IOException {
        super(reader);
        m_currentVersion = currentVersion;
    }

    /**
     * <p>Constructor for LinkdConfigFactory.</p>
     *
     * @param currentVersion a long.
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public LinkdConfigFactory(final long currentVersion, final InputStream stream) throws MarshalException, ValidationException, IOException {
        super(stream);
        m_currentVersion = currentVersion;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
        LogUtils.debugf(LinkdConfigFactory.class, "init: config file path: %s", cfgFile.getPath());

        InputStream stream = null;
        try {
            stream = new FileInputStream(cfgFile);
            setInstance(new LinkdConfigFactory(cfgFile.lastModified(), stream));
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        init();
        getInstance().update();
    }

    /** {@inheritDoc} */
    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            long timestamp = System.currentTimeMillis();
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
            LogUtils.debugf(LinkdConfigFactory.class, "saveXml: saving config file at %d: %s", timestamp, cfgFile.getPath());
            final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            LogUtils.debugf(LinkdConfigFactory.class, "saveXml: finished saving config file: %s", cfgFile.getPath());
        }
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized LinkdConfig getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.LinkdConfig} object.
     */
    public static synchronized void setInstance(final LinkdConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void update() throws IOException, MarshalException, ValidationException {
        getWriteLock().lock();
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
            if (cfgFile.lastModified() > m_currentVersion) {
                m_currentVersion = cfgFile.lastModified();
                LogUtils.debugf(this, "init: config file path: %s", cfgFile.getPath());
                InputStream stream = null;
                try {
                    stream = new FileInputStream(cfgFile);
                    reloadXML(stream);
                } finally {
                    if (stream != null) {
                        IOUtils.closeQuietly(stream);
                    }
                }
                LogUtils.debugf(this, "init: finished loading config file: %s", cfgFile.getPath());
            }
        } finally {
            getWriteLock().unlock();
        }
    }
}
