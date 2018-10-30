/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.daemon;

import java.util.function.Consumer;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaemonTools {
    /**
     * Interface to accept the implementation of configuration reloading.
     * This is used instead of {@link Consumer} as it allows throwing Exceptions
     */
    public interface Action<T> {
        void accept(T t) throws Exception;
    }

    public static final Logger LOG = LoggerFactory.getLogger(DaemonTools.class);

    /**
     * @see #handleReloadEvent(Event, String, String, Action, Consumer)
     */
    public static void handleReloadEvent(Event e, String daemonName, Action<Event> handleConfigurationChanged) {
        handleReloadEvent(e, daemonName, null, (event) -> handleConfigurationChanged.accept(event), null);
    }

    /**
     * @see #handleReloadEvent(Event, String, String, Action, Consumer)
     */
    public static void handleReloadEvent(Event e, String daemonName, Action<Event> handleConfigurationChanged, Consumer<Exception> exceptionHandler) {
        handleReloadEvent(e, daemonName, null, (event) -> handleConfigurationChanged.accept(event), (exception) -> exceptionHandler.accept(exception));
    }

    /**
     * @see #handleReloadEvent(Event, String, String, Action, Consumer)
     */
    public static void handleReloadEvent(Event e, String daemonName, String targetFile, Action<Event> handleConfigurationChanged) {
        handleReloadEvent(e, daemonName, targetFile, (event) -> handleConfigurationChanged.accept(event), null);
    }

    /**
     * Handles the ReloadEvent of a daemon, to have a standardized way they are done.
     *
     * In order to trigger a reload the following conditions must be fulfilled:
     *  - The event's uei must match {@link EventConstants#RELOAD_DAEMON_CONFIG_UEI}
     *  - The event must contain a parameter {@link EventConstants#PARM_DAEMON_NAME} which matches the provided <code>daemonName</code>
     *
     * Afterwards an event with uei {@link EventConstants#RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI} or {@link EventConstants#RELOAD_DAEMON_CONFIG_FAILED_UEI}
     * (depending on success or failure of the reload) is send.
     *
     *
     * @param event                      the Reload Event with a uei {@link EventConstants#RELOAD_DAEMON_CONFIG_UEI}.
     * @param daemonName                 the Name of the daemon that should be updated, needed for the Name-check with the event-parameter
     * @param targetFile                 the Name and path of the configfile, that will be reloaded
     * @param handleConfigurationChanged An Action that handles the Configuration change (Can throw Exceptions)
     * @param exceptionHandler           A Consumer that handles possible Exceptions from the given handleConfigurationChanged-Action (Can not throw Exceptions)
     */
    public static void handleReloadEvent(Event event, String daemonName, String targetFile, Action<Event> handleConfigurationChanged, Consumer<Exception> exceptionHandler) {
        if (!EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {
            LOG.warn("The event does not have the correct uei: {}. Ignoring.", EventConstants.RELOAD_DAEMON_CONFIG_UEI);
            return;
        }

        final Parm daemonNameParm = event.getParm(EventConstants.PARM_DAEMON_NAME);
        if (daemonNameParm == null || daemonNameParm.getValue() == null) {
            LOG.warn("The {} parameter has no value. Ignoring.", EventConstants.PARM_DAEMON_NAME);
            return;
        }

        if (daemonName.equalsIgnoreCase(daemonNameParm.getValue().getContent())) {
            LOG.info("Reloading {}.", daemonName);

            EventBuilder ebldr = null;
            try {
                handleConfigurationChanged.accept(event);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, daemonName);
                LOG.info("Reload successful.");
            } catch (Exception t) {
                if (exceptionHandler != null) {
                    exceptionHandler.accept(t);
                }
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, daemonName);
                ebldr.addParam(EventConstants.PARM_REASON, t.getLocalizedMessage().substring(0, 128));
                LOG.error("Reload failed.", t);
            }

            if (targetFile != null) {
                ebldr.addParam(EventConstants.PARM_CONFIG_FILE_NAME, targetFile);
            }
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
            EventIpcManagerFactory.getIpcManager().sendNow(ebldr.getEvent());
        }
    }

}
