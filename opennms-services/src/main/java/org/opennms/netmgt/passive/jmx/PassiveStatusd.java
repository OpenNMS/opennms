/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created September 30, 2005
 *
 * Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.passive.jmx;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.passive.PassiveStatusKeeper;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class PassiveStatusd extends AbstractServiceDaemon implements PassiveStatusdMBean {

    public PassiveStatusd() {
        super(NAME);
    }

    public final static String NAME = "OpenNMS.PassiveStatus";

    protected void onInit() {
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
        try {
            DataSourceFactory.init();
        } catch (MarshalException e) {
            log.error("Could not unmarshall configuration", e);
        } catch (ValidationException e) {
            log.error("validation error ", e);
        } catch (IOException e) {
            log.error("IOException: ", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to initialize database: "+e.getMessage(), e);
        } catch (SQLException e) {
            log.error("SQLException: ", e);
        } catch (PropertyVetoException e) {
            log.error("PropertyVetoException: "+e.getMessage(), e);
        }
        // XXX We don't throw an exception?
        
        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();

        PassiveStatusKeeper keeper = getPassiveStatusKeeper();
        keeper.setEventManager(mgr);
        keeper.setDataSource(DataSourceFactory.getInstance());
        keeper.init();
    }

    protected void onStart() {
        getPassiveStatusKeeper().start();
    }

    protected void onStop() {
        getPassiveStatusKeeper().stop();
    }

    public int getStatus() {
        return getPassiveStatusKeeper().getStatus();
    }

    private PassiveStatusKeeper getPassiveStatusKeeper() {
        return PassiveStatusKeeper.getInstance();
    }
}
