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
 * <p>NotifdConfigFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
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
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotifdConfigFactory} object.
     */
    static synchronized public NotifdConfigFactory getInstance() {
        if (!initialized)
            throw new IllegalStateException("init() not called.");

        return instance;
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
        if (!initialized) {
            instance = new NotifdConfigFactory();
            instance.reload();
            initialized = true;
        }
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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

    /** {@inheritDoc} */
    @Override
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_notifdConfFile), "UTF-8");
            fileWriter.write(xml);
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
    @Override
    protected synchronized void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_notifdConfFile.lastModified()) {
            NotifdConfigFactory.getInstance().reload();
        }
    }


}
