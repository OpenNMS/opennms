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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>UserFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class UserFactory extends UserManager {
    /**
     * The static singleton instance of the UserFactory
     */
    private static UserManager instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private File m_usersConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * 
     */
    private long m_fileSize;

    /**
     * Initializes the factory
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public UserFactory() throws  FileNotFoundException, IOException {
        super(GroupFactory.getInstance());
        reload();
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException {
        
        if (instance == null || !initialized) {
            GroupFactory.init();
            instance = new UserFactory();
            initialized = true;
        }

    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * UserFactory
     *
     * @return the single user factory instance
     */
    public static synchronized UserManager getInstance() {
        return instance;
    }
    
    /**
     * <p>Setter for the field <code>instance</code>.</p>
     *
     * @param mgr a {@link org.opennms.netmgt.config.UserManager} object.
     */
    public static synchronized void setInstance(UserManager mgr) {
        initialized = true;
        instance = mgr;
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public void reload() throws IOException, FileNotFoundException {
        // Form the complete filename for the config file
        //
        m_usersConfFile = ConfigFileConstants.getFile(ConfigFileConstants.USERS_CONF_FILE_NAME);

        InputStream configIn = new FileInputStream(m_usersConfFile);
        m_lastModified = m_usersConfFile.lastModified();
        m_fileSize = m_usersConfFile.length();

        parseXML(configIn);
        
        initialized = true;

    }

    /** {@inheritDoc} */
    @Override
    protected void saveXML(String writerString) throws IOException {
        if (writerString != null) {
            Writer fileWriter = null;
            try {
                fileWriter = new OutputStreamWriter(new FileOutputStream(m_usersConfFile), StandardCharsets.UTF_8);
                fileWriter.write(writerString);
                fileWriter.flush();
            } finally {
                IOUtils.closeQuietly(fileWriter);
            }
        }
    }

    /**
     * <p>isUpdateNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isUpdateNeeded() {
        if (m_usersConfFile == null) {
            return true;
        } else {
            final long fileLastModified = m_usersConfFile.lastModified();

            // Check to see if the file size has changed
            if (m_fileSize != m_usersConfFile.length()) {
                return true;
            // Check to see if the timestamp has changed
            } else if (m_lastModified != fileLastModified) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    @Override
    public void doUpdate() throws IOException, FileNotFoundException {
        if (isUpdateNeeded()) {
            reload();
        }
    }

    @Override
    public long getLastModified() {
        return m_lastModified;
    }

    @Override
    public long getFileSize() {
        return m_fileSize;
    }
}
