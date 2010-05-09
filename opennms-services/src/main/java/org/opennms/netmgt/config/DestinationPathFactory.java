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
// 2003 Jan 31: Cleaned up some unused imports.
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
import org.opennms.netmgt.ConfigFileConstants;

/**
 */
public class DestinationPathFactory extends DestinationPathManager {
    /**
     * 
     */
    private static DestinationPathFactory instance;

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static File m_notifConfFile;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private static File m_pathsConfFile;

    /**
     * 
     */
    private static long m_lastModified;

    /**
     * 
     */
    public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        if (!initialized) {
            getInstance().reload();
            initialized = true;
        }
    }

    /**
     * 
     */
    public static synchronized DestinationPathFactory getInstance() {

        if (instance == null || !initialized) {
            instance = new DestinationPathFactory();
        }

        return instance;
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws MarshalException
     * @throws ValidationException
     */
    public void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        m_pathsConfFile = ConfigFileConstants.getFile(ConfigFileConstants.DESTINATION_PATHS_CONF_FILE_NAME);

        InputStream configIn = new FileInputStream(m_pathsConfFile);
        m_lastModified = m_pathsConfFile.lastModified();

        parseXML(configIn);
        configIn.close();
    }

    /**
     * @param writerString
     * @throws IOException
     */
    protected void saveXML(String writerString) throws IOException {
        if (writerString != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_pathsConfFile), "UTF-8");
            fileWriter.write(writerString);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     * @throws FileNotFoundException
     */
    public void update() throws IOException, MarshalException, ValidationException, FileNotFoundException {
        if (m_lastModified != m_pathsConfFile.lastModified()) {
            reload();
        }
    }
}
