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

package org.opennms.netmgt.translator.jmx;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.EventTranslatorConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventIpcManager;

/**
 * <p>EventTranslator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class EventTranslator extends AbstractServiceDaemon implements EventTranslatorMBean {

    /**
     * <p>Constructor for EventTranslator.</p>
     */
    public EventTranslator() {
        super(NAME);
    }

    /** Constant <code>NAME="OpenNMS.EventTranslator"</code> */
    public final static String NAME = "OpenNMS.EventTranslator";

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
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

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
        getEventTranslator().start();
    }

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
        getEventTranslator().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    public int getStatus() {
        return getEventTranslator().getStatus();
    }

    private org.opennms.netmgt.translator.EventTranslator getEventTranslator() {
        return org.opennms.netmgt.translator.EventTranslator.getInstance();
    }
}
