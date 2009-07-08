/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;

import org.opennms.netmgt.ConfigFileConstants;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

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

        File rootConfigFile = getDefaultRootConfigFile();

        DefaultEventConfDao newInstance = new DefaultEventConfDao();
        newInstance.setConfigResource(new FileSystemResource(rootConfigFile));
        newInstance.afterPropertiesSet();

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
    
    private static File getDefaultRootConfigFile() throws DataAccessException {
        try {
            return ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME);
        } catch (IOException e) {
            throw new ObjectRetrievalFailureException(String.class, ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME), "Could not get configuration file for " + ConfigFileConstants.getFileName(ConfigFileConstants.EVENT_CONF_FILE_NAME), e);
        }
    }
}

