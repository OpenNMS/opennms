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

package org.opennms.netmgt.actiond.jmx;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.netmgt.config.ActiondConfigFactory;

/**
 * <p>Actiond class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Actiond implements ActiondMBean {
    /**
     * <p>init</p>
     */
    public void init() {
    	
    	try {
			ActiondConfigFactory.init();
		} catch (Exception e) {
			throw new UndeclaredThrowableException(e);
		}
    	ActiondConfigFactory actiondConfig = ActiondConfigFactory.getInstance();
    	
        org.opennms.netmgt.actiond.Actiond actiond = org.opennms.netmgt.actiond.Actiond.getInstance();
        actiond.setActiondConfig(actiondConfig);
        actiond.init();
    }

    /**
     * <p>start</p>
     */
    public void start() {
        org.opennms.netmgt.actiond.Actiond actiond = org.opennms.netmgt.actiond.Actiond.getInstance();
        actiond.start();
    }

    /**
     * <p>stop</p>
     */
    public void stop() {
        org.opennms.netmgt.actiond.Actiond actiond = org.opennms.netmgt.actiond.Actiond.getInstance();
        actiond.stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        org.opennms.netmgt.actiond.Actiond actiond = org.opennms.netmgt.actiond.Actiond.getInstance();
        return actiond.getStatus();
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusText() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }
}
