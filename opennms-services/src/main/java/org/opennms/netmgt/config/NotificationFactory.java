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
// 2004 Nov 22: Fixed problem with notifications for threshold events on non-IP interfaces.
// 2003 Aug 01: Created a proper JOIN for notifications. Bug #752
// 2003 Jan 31: Added an ORDER BY clause. Bug #648
// 2003 Jan 08: Allow notification where nodeid, interfaceid and/or serviceid are null.
// 2002 Nov 13: Corrected a small bug with notifications.
// 2002 Nov 13: Added two new files for notifications, nodelabel and interfaceresolve.
// 2002 Nov 09: Added the ability to map a single event to multiple notifications.
// 2002 Oct 30: Modified filter rules to work on node, interface and/or service.
// 2002 Jul 08: Corrected SELECT statement to correct return acknowledged notifications.
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

package org.opennms.netmgt.config;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_noticeConfFile), "UTF-8");
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * 
     */
    public void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_noticeConfFile.lastModified()) {
            reload();
        }
    }
}
