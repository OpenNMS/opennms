/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.opennms.core.utils.PropertiesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Maintains state in cfg file:
 *   systemid=000-00-00-0000
 *   enabled=true
 *   acknowledged-by=admin
 *   acknowledged-at=13943454545
 *
 * Same file also holds:
 *   url=http://stats.opennms.org/datachoices/
 *   interval=86400000
 *
 * @author jwhite
 */
public class StateManager {
    private static final Logger LOG = LoggerFactory.getLogger(StateManager.class);

    private static final String PROPERTIES_FILE_NAME = "org.opennms.features.datachoices.cfg";
    private static final String ENABLED_KEY = "enabled";
    private static final String SYSTEM_ID_KEY = "systemid";
    private static final String ACKNOWLEDGED_BY_KEY = "acknowledged-by";
    private static final String ACKNOWLEDGED_AT_KEY = "acknowledged-at";

    private final List<StateChangeHandler> m_listeners = Lists.newArrayList();
    private final PropertiesCache m_propertiesCache = new PropertiesCache();
    private final File m_propertiesFile;

    public static interface StateChangeHandler {
        void onIsEnabledChanged(boolean isEnabled);
    }

    public StateManager() {
        String opennmsHomeStr = System.getProperty("opennms.home");
        if (opennmsHomeStr == null) {
            // Use working directory
            opennmsHomeStr = "";
        }
        m_propertiesFile = Paths.get(opennmsHomeStr, "etc", PROPERTIES_FILE_NAME).toFile();
    }

    public Boolean isEnabled() throws IOException {
        final String enabled = m_propertiesCache.getProperty(m_propertiesFile, ENABLED_KEY);
        return enabled == null ? null : Boolean.valueOf(enabled);
    }

    public void setEnabled(boolean enabled, String user) throws Exception {
        m_propertiesCache.setProperty(m_propertiesFile, ENABLED_KEY, Boolean.valueOf(enabled).toString());
        m_propertiesCache.setProperty(m_propertiesFile, ACKNOWLEDGED_BY_KEY, user == null ? "" : user);
        m_propertiesCache.setProperty(m_propertiesFile, ACKNOWLEDGED_AT_KEY, new Date().toString());
        for (StateChangeHandler listener : m_listeners) {
            listener.onIsEnabledChanged(enabled);
        }
    }

    public String getOrGenerateSystemId() throws IOException {
        String systemId = m_propertiesCache.getProperty(m_propertiesFile, SYSTEM_ID_KEY);
        if (systemId == null) {
            LOG.debug("No existing system id was found. Generating a new system id.");
            systemId = UUID.randomUUID().toString();
            m_propertiesCache.setProperty(m_propertiesFile, SYSTEM_ID_KEY, systemId);
        }
        return systemId;
    }

    public String getAndRegenerateSystemId() throws IOException {
        String systemId = UUID.randomUUID().toString();
        m_propertiesCache.setProperty(m_propertiesFile, SYSTEM_ID_KEY, systemId);
        return systemId;
    }

    public void onIsEnabledChanged(StateChangeHandler callback) {
        m_listeners.add(Objects.requireNonNull(callback));
    }
}
