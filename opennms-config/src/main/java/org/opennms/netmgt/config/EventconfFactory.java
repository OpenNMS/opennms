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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;

import org.opennms.core.utils.ConfigFileConstants;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * <p>EventconfFactory class.</p>
 * 
 * @deprecated This class is just a thin wrapper around an {@link EventConfDao}
 * instance. Instead of using this factory, you should use dependency injection
 * to acquire a reference to the {@link EventConfDao} implementation instance.
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class EventconfFactory {
    /**
     * The static singleton instance of the EventConfDao.
     * Is null if the init() method has not been called.
     */
    private static EventConfDao s_instance;

    /**
     * <p>init</p>
     *
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public static synchronized void init() throws DataAccessException {
        if (isInitialized()) {
            return;
        }

        File rootConfigFile = getDefaultRootConfigFile();

        DefaultEventConfDao newInstance = new DefaultEventConfDao();
        newInstance.setConfigResource(new FileSystemResource(rootConfigFile));
        newInstance.afterPropertiesSet();

        setInstance(newInstance);
    }

    /**
     * A full reinitialization, from scratch.  Subtly different from a reload (more encompassing).
     * Safe to call in place of init if you so desire
     *
     * @throws org.springframework.dao.DataAccessException if any.
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

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.EventConfDao} object.
     */
    public static void setInstance(EventConfDao instance) {
        s_instance = instance;
    }

    private static boolean isInitialized() {
        return s_instance != null;
    }
    
    private static File getDefaultRootConfigFile() throws DataAccessException {
        try {
            return ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
        } catch (IOException e) {
            throw new ObjectRetrievalFailureException(String.class, ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME), "Could not get configuration file for " + ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME), e);
        }
    }
}

