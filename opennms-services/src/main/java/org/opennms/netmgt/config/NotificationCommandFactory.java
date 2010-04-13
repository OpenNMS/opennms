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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;

/**
 */
public class NotificationCommandFactory extends NotificationCommandManager {
    /**
     */
    private static NotificationCommandFactory instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private NotificationCommandFactory() {
    }

    /**
     * 
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (!initialized) {
            getInstance().update();
            initialized = true;
        }
    }

    /**
     */
    public static synchronized NotificationCommandFactory getInstance() {

        if (instance == null || !initialized) {
            instance = new NotificationCommandFactory();
        }

        return instance;
    }
    
    public void update() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        InputStream configIn = new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.NOTIF_COMMANDS_CONF_FILE_NAME));
        parseXML(configIn);
    }
}
