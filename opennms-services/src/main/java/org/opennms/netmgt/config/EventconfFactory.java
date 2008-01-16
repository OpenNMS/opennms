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
// 2008 Jan 06: Pull non-static code into DefaultEventConfDao. - dj@opennms.org
// 2008 Jan 06: Duplicate all EventConfigurationManager functionality in
//              EventconfFactory. - dj@opennms.org
// 2008 Jan 05: Add a few new constructors and make them all public,
//              eliminate static fields except for s_instance. - dj@opennms.org
// 2008 Jan 05: Simplify init()/reload()/getInstance(). - dj@opennms.org
// 2008 Jan 05: Organize imports, format code, refactor some, and line up some
//              functionality with EventConfigurationManager. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 29: Added include files for eventconf.xml
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

import org.springframework.dao.DataAccessException;

/**
 */
public class EventconfFactory {
    /**
     * The static singleton instance of the EventConfDao.
     * Is null if the init() method has not been called.
     */
    private static EventConfDao s_instance;

    /**
     * No constructors, only static methods.  Thank you, drive through.
     */
    private EventconfFactory() {
        
    }
    
    /**
     * 
     */
    public static synchronized void init() throws DataAccessException {
        if (isInitialized()) {
            return;
        }

        EventConfDao newInstance = new DefaultEventConfDao();
        newInstance.reload();

        setInstance(newInstance);
    }

    /**
     * A full reinitialization, from scratch.  Subtly different from a reload (more encompassing).
     * Safe to call in place of init if you so desire
     */
    public static synchronized void reinit() throws DataAccessException {
        setInstance(null);
        init();
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * EventConfDao.
     * 
     * @return the single eventconf factory instance
     */
    public static synchronized EventConfDao getInstance() {
        if (!isInitialized()) {
            throw new IllegalStateException("init() or setInstance() not called.");
        }

        return s_instance;
    }

    public static void setInstance(EventConfDao instance) {
        s_instance = instance;
    }

    private static boolean isInitialized() {
        return s_instance != null;
    }
}

