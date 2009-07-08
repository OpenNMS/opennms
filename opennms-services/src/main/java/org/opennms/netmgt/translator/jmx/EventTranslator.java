/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.translator.jmx;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.EventTranslatorConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class EventTranslator implements EventTranslatorMBean {

    public final static String LOG4J_CATEGORY = "OpenNMS.EventTranslator";

    public void init() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);


        Category log = ThreadCategory.getInstance();
        try {
            DataSourceFactory.init();
            EventTranslatorConfigFactory.init();
        } catch (MarshalException e) {
            log.error("Could not unmarshall configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log.error("validation error ", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log.error("IOException: ", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to initialize database: "+e.getMessage(), e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log.error("SQLException: ", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            log.error("PropertyVetoException: ", e);
            throw new UndeclaredThrowableException(e);
        }
        
        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();

        org.opennms.netmgt.translator.EventTranslator keeper = getEventTranslator();
        keeper.setConfig(EventTranslatorConfigFactory.getInstance());
        keeper.setEventManager(mgr);
        keeper.setDataSource(DataSourceFactory.getInstance());
        keeper.init();
    }

    public void start() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        getEventTranslator().start();
    }

    public void stop() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        getEventTranslator().stop();
    }

    public int getStatus() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return getEventTranslator().getStatus();
    }

    public String status() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    public String getStatusText() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    private org.opennms.netmgt.translator.EventTranslator getEventTranslator() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return org.opennms.netmgt.translator.EventTranslator.getInstance();
    }


}
