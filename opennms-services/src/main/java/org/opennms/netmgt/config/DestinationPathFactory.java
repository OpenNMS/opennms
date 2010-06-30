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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * <p>DestinationPathFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DestinationPathFactory extends DestinationPathManager {
    /**
     * 
     */
    private static DestinationPathFactory instance;

    /**
     * 
     */
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
     * <p>Constructor for DestinationPathFactory.</p>
     */
    public DestinationPathFactory() {
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
            getInstance().reload();
            initialized = true;
        }
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.DestinationPathFactory} object.
     */
    public static synchronized DestinationPathFactory getInstance() {

        if (instance == null || !initialized) {
            instance = new DestinationPathFactory();
        }

        return instance;
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
        m_pathsConfFile = ConfigFileConstants.getFile(ConfigFileConstants.DESTINATION_PATHS_CONF_FILE_NAME);

        InputStream configIn = new FileInputStream(m_pathsConfFile);
        m_lastModified = m_pathsConfFile.lastModified();

        Reader reader = new InputStreamReader(configIn);
        parseXML(reader);
    }

    /** {@inheritDoc} */
    protected void saveXML(String writerString) throws IOException {
        if (writerString != null) {
            FileWriter fileWriter = new FileWriter(m_pathsConfFile);
            fileWriter.write(writerString);
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
     * @throws java.io.FileNotFoundException if any.
     */
    public void update() throws IOException, MarshalException, ValidationException, FileNotFoundException {
        if (m_lastModified != m_pathsConfFile.lastModified()) {
            reload();
        }
    }
}
