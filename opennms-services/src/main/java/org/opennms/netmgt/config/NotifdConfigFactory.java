/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
/**
 */
public class NotifdConfigFactory extends NotifdConfigManager {
    /**
     * Singleton instance
     */
    private static NotifdConfigFactory instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private File m_notifdConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * 
     */
    private NotifdConfigFactory() {
    }

    /**
     * 
     */
    static synchronized public NotifdConfigFactory getInstance() {
        if (!initialized)
            throw new IllegalStateException("init() not called.");

        return instance;
    }

    /**
     * 
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (!initialized) {
            instance = new NotifdConfigFactory();
            instance.reload();
            initialized = true;
        }
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws MarshalException
     * @throws ValidationException
     */
    public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        m_notifdConfFile = ConfigFileConstants.getFile(ConfigFileConstants.NOTIFD_CONFIG_FILE_NAME);

        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_notifdConfFile);
            m_lastModified = m_notifdConfFile.lastModified();
            parseXml(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /**
     * Gets a nicely formatted string for the Web UI to display
     * 
     * @return On, Off, or Unknown depending on status
     * 
     * TODO: Pull up into base class but keep this reference for the
     * webapp until singleton is removed.
     */
    public static String getPrettyStatus() throws IOException, MarshalException, ValidationException {
        if (!initialized)
            return "Unknown";

        String status = "Unknown";

        status = NotifdConfigFactory.getInstance().getNotificationStatus();

        if (status.equals("on"))
            status = "On";
        else if (status.equals("off"))
            status = "Off";

        return status;
    }

    /**
     * Turns the notifd service on
     * TODO: this was pulled up into the base class but is still here
     * because of a reference from the webapp.  Fix up by renaming the
     * method in the base class can calling that method from here.
     */
    public void turnNotifdOn() throws MarshalException, ValidationException, IOException {
        sendEvent("uei.opennms.org/internal/notificationsTurnedOn");
        configuration.setStatus("on");

        saveCurrent();
    }

    /**
     * Turns the notifd service off
     * TODO: this was pulled up into the base class but is still here
     * because of a reference from the webapp.  Fix up by renaming the
     * method in the base class can calling that method from here.
     */
    public void turnNotifdOff() throws MarshalException, ValidationException, IOException {
        sendEvent("uei.opennms.org/internal/notificationsTurnedOff");
        configuration.setStatus("off");

        saveCurrent();
    }

    /**
     * @param xml
     * @throws IOException
     */
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            FileWriter fileWriter = new FileWriter(m_notifdConfFile);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * 
     */
    protected synchronized void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_notifdConfFile.lastModified()) {
            NotifdConfigFactory.getInstance().reload();
        }
    }


}
