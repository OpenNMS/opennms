/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
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

    // private static ViewFactory viewFactory;

    /**
     * File path of users.xml
     */
    protected File usersFile;

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
     * Initializes the factory
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public UserFactory() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        super(GroupFactory.getInstance());
        reload();
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        
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
    static synchronized public UserManager getInstance() {
        return instance;
    }
    
    /**
     * <p>Setter for the field <code>instance</code>.</p>
     *
     * @param mgr a {@link org.opennms.netmgt.config.UserManager} object.
     */
    static synchronized public void setInstance(UserManager mgr) {
        initialized = true;
        instance = mgr;
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        // Form the complete filename for the config file
        //
        m_usersConfFile = ConfigFileConstants.getFile(ConfigFileConstants.USERS_CONF_FILE_NAME);

        InputStream configIn = new FileInputStream(m_usersConfFile);
        m_lastModified = m_usersConfFile.lastModified();

        parseXML(configIn);
        
        initialized = true;

    }

    /** {@inheritDoc} */
    protected void saveXML(String writerString) throws IOException {
        if (writerString != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_usersConfFile), "UTF-8");
            fileWriter.write(writerString);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * <p>isUpdateNeeded</p>
     *
     * @return a boolean.
     */
    public boolean isUpdateNeeded() {
        if (m_usersConfFile == null) {
            return true;
        }
        if (m_lastModified != m_usersConfFile.lastModified()) {
            return true;
        }
        return false;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void doUpdate() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (isUpdateNeeded()) {
            reload();
        }
    }

    @Override
    public long getLastModified() {
        return m_lastModified;
    }
}
