//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 24: Changed all references to HastTable to HashMap.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
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
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This is the singleton class used to load the configuration for the poller
 * outages from the poll-outages xml file. <strong>Note: </strong>Users of
 * this class should make sure the <em>init()</em> is called before calling
 * any other method to ensure the config is loaded before accessing other
 * convenience methods.
 * 
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class PollOutagesConfigFactory extends PollOutagesConfigManager {
    /**
     * The singleton instance of this factory
     */
    private static PollOutagesConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    PollOutagesConfigFactory(final String configFile) {
        setConfigResource(new FileSystemResource(configFile));
    }

    /**
     * Create a PollOutagesConfigFactory using the specified Spring resource.
     */
    public PollOutagesConfigFactory(final Resource resource) {
        setConfigResource(resource);
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
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        m_singleton = new PollOutagesConfigFactory(new FileSystemResource(ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME)));
        m_singleton.afterPropertiesSet();
        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be
     *                read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public static void reload() throws IOException, MarshalException, ValidationException {
        init();
        getInstance().update();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static PollOutagesConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * <p>
     * setInstance
     * </p>
     * 
     * @param instance
     *            a {@link org.opennms.netmgt.config.PollOutagesConfigFactory}
     *            object.
     */
    public static void setInstance(final PollOutagesConfigFactory instance) {
        m_loaded = true;
        m_singleton = instance;

    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     * 
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public void saveCurrent() throws MarshalException, IOException, ValidationException {
        getWriteLock().lock();

        try {
            // Marshal to a string first, then write the string to the file.
            // This
            // way the original configuration isn't lost if the XML from the
            // marshal is hosed.
            StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(getConfig(), stringWriter);

            String xmlString = stringWriter.toString();
            if (xmlString != null) {
                saveXML(xmlString);
            }
        } finally {
            getWriteLock().unlock();
        }

        update();
    }

    /** {@inheritDoc} */
    protected void saveXML(final String xmlString) throws IOException, MarshalException, ValidationException {
        getWriteLock().lock();

        try {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME);
    
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * <p>
     * update
     * </p>
     * 
     * @throws java.io.IOException
     *             if any.
     * @throws org.exolab.castor.xml.MarshalException
     *             if any.
     * @throws org.exolab.castor.xml.ValidationException
     *             if any.
     */
    public void update() throws IOException, MarshalException, ValidationException {
        getReadLock().lock();
        try {
            getContainer().reload();
        } finally {
            getReadLock().unlock();
        }
    }
}
