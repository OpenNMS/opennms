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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//
package org.opennms.netmgt.outage.jmx;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.OutageManagerConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.outage.OutageManager;

public class Outaged implements OutagedMBean {
    
    public void init() {
        
        Category log = ThreadCategory.getInstance(getClass());
        
        EventIpcManagerFactory.init();
        EventIpcManager eventMgr = EventIpcManagerFactory.getIpcManager();
        getOutageManager().setEventMgr(eventMgr);
        
        
        try {
            OutageManagerConfigFactory.reload();
            getOutageManager().setOutageMgrConfig(OutageManagerConfigFactory.getInstance());

            DatabaseConnectionFactory.init();

        } catch (MarshalException ex) {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ClassNotFoundException ex) {
            log.error("Failed to load database connection factory configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
        
        
        getOutageManager().setDbConnectionFactory(DatabaseConnectionFactory.getInstance());
        getOutageManager().init();
    }

    public void start() {
        getOutageManager().start();
    }

    public void stop() {
        getOutageManager().stop();
    }

    public int getStatus() {
        return getOutageManager().getStatus();
    }

    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    public String getStatusText() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }
    
    private OutageManager getOutageManager() {
        return OutageManager.getInstance();
    }


}
