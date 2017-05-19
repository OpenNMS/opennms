/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.scriptd.Engine;
import org.opennms.netmgt.config.scriptd.EventScript;
import org.opennms.netmgt.config.scriptd.ReloadScript;
import org.opennms.netmgt.config.scriptd.ScriptdConfiguration;
import org.opennms.netmgt.config.scriptd.StartScript;
import org.opennms.netmgt.config.scriptd.StopScript;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Scriptd from the scriptd-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class ScriptdConfigFactory {
    /**
     * The singleton instance of this factory
     */
    private static ScriptdConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private ScriptdConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     */
    private ScriptdConfigFactory(String configFile) throws IOException {
        m_config = JaxbUtils.unmarshal(ScriptdConfiguration.class, new FileSystemResource(configFile));
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SCRIPTD_CONFIG_FILE_NAME);
        m_singleton = new ScriptdConfigFactory(cfgFile.getPath());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized ScriptdConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Return the array of configured engines.
     *
     * @return the array of configured engines
     */
    public synchronized List<Engine> getEngines() {
        return m_config.getEngines();
    }

    /**
     * Return the array of start scripts.
     *
     * @return the array of start scripts
     */
    public synchronized List<StartScript> getStartScripts() {
        return m_config.getStartScripts();
    }

    /**
     * Return the array of stop scripts.
     *
     * @return the array of stop scripts
     */
    public synchronized List<StopScript> getStopScripts() {
        return m_config.getStopScripts();
    }

    /**
     * Return the array of reload scripts.
     *
     * @return the array of reload scripts
     */
    public synchronized List<ReloadScript> getReloadScripts() {
        return m_config.getReloadScripts();
    }

    /**
     * Return the array of configured event scripts.
     *
     * @return the array of configured event scripts
     */
    public synchronized List<EventScript> getEventScripts() {
        return m_config.getEventScripts();
    }
}
