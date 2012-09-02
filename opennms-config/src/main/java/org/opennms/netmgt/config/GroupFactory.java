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

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>GroupFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GroupFactory extends GroupManager {
    /**
     * The static singleton instance object
     */
    private static GroupManager s_instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean s_initialized = false;

    /**
     * 
     */
    private File m_groupsConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * Constructor which parses the file
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     */
    public GroupFactory() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        super();
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

        if (s_instance == null || !s_initialized) {
            s_instance = new GroupFactory();
            s_initialized = true;
        }

    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * GroupFactory
     *
     * @return the single group factory instance
     */
    public static synchronized GroupManager getInstance() {
        return s_instance;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param mgr a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public static synchronized void setInstance(GroupManager mgr) {
        s_initialized = true;
        s_instance = mgr;
    }

    /**
     * Parses the groups.xml via the Castor classes
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        File confFile = ConfigFileConstants.getFile(ConfigFileConstants.GROUPS_CONF_FILE_NAME);

        reloadFromFile(confFile);
    }

    /**
     * @param confFile
     * @throws FileNotFoundException
     * @throws MarshalException
     * @throws ValidationException
     */
    private void reloadFromFile(File confFile) throws FileNotFoundException, MarshalException, ValidationException {
        m_groupsConfFile = confFile;
        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_groupsConfFile);
            m_lastModified = m_groupsConfFile.lastModified();
            parseXml(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /** {@inheritDoc} */
    protected void saveXml(String data) throws IOException {
        if (data != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_groupsConfFile), "UTF-8");
            fileWriter.write(data);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_groupsConfFile.lastModified()) {
            reload();
        }
    }
}
