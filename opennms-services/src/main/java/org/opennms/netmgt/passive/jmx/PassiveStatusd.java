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

package org.opennms.netmgt.passive.jmx;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.passive.PassiveStatusKeeper;

/**
 * <p>PassiveStatusd class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class PassiveStatusd extends AbstractServiceDaemon implements PassiveStatusdMBean {

    /**
     * <p>Constructor for PassiveStatusd.</p>
     */
    public PassiveStatusd() {
        super(NAME);
    }

    /** Constant <code>NAME="OpenNMS.PassiveStatus"</code> */
    public final static String NAME = "OpenNMS.PassiveStatusKeeper";

    /**
     * <p>onInit</p>
     */
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

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
        getPassiveStatusKeeper().start();
    }

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
        getPassiveStatusKeeper().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return getPassiveStatusKeeper().getStatus();
    }

    private PassiveStatusKeeper getPassiveStatusKeeper() {
        return PassiveStatusKeeper.getInstance();
    }
}
