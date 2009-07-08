/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;

/**
 */
public class NotificationFactory extends NotificationManager {
    /**
     * Singleton instance
     */
    private static NotificationFactory instance;

    /**
     * Configuration file handle
     */
    protected File m_notifConfFile;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private File m_noticeConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * 
     */
    private NotificationFactory() {
        super(NotifdConfigFactory.getInstance(), DataSourceFactory.getInstance());
    }

    /**
     * 
     */
    static synchronized public NotificationFactory getInstance() {
        if (!initialized)
            return null;

        return instance;
    }

    /**
     * 
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException, ClassNotFoundException, SQLException, PropertyVetoException  {
        if (!initialized) {
            DataSourceFactory.init();
            instance = new NotificationFactory();
            instance.reload();
            initialized = true;
        }
    }

    /**
     * 
     */
    public synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_noticeConfFile = ConfigFileConstants.getFile(ConfigFileConstants.NOTIFICATIONS_CONF_FILE_NAME);

        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_noticeConfFile);
            m_lastModified = m_noticeConfFile.lastModified();
            parseXML(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /**
     * @param xmlString
     * @throws IOException
     */
    protected void saveXML(String xmlString) throws IOException {
        if (xmlString != null) {
            FileWriter fileWriter = new FileWriter(m_noticeConfFile);
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * 
     */
    protected void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_noticeConfFile.lastModified()) {
            reload();
        }
    }
}
