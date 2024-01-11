/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.util.CmProperties;
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

    private static final String PROPERTIES_CONFIG_NAME = "org.opennms.features.datachoices";

    /**
     * Whether data choices / Usage Statistics Sharing is enabled.
     */
    private static final String ENABLED_KEY = "enabled";
    private static final String SYSTEM_ID_KEY = "systemid";
    private static final String ACKNOWLEDGED_BY_KEY = "acknowledged-by";
    private static final String ACKNOWLEDGED_AT_KEY = "acknowledged-at";

    /**
     * Whether the Usage Statistics Sharing notice was acknowledged.
     */
    private static final String INITIAL_NOTICE_ACKNOWLEDGED_KEY = "initialNoticeAcknowledged";
    private static final String INITIAL_NOTICE_ACKNOWLEDGED_AT_KEY = "initialNoticeAcknowledgedAt";
    private static final String INITIAL_NOTICE_ACKNOWLEDGED_BY_KEY = "initialNoticeAcknowledgedBy";

    /**
     * Whether a user opted-in to User Data Collection.
     */
    private static final String USER_DATA_COLLECTION_OPTED_IN_KEY = "userDataCollectionOptedIn";

    /**
     * Whether the User Data Collection notice was acknowledged.
     */
    private static final String USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_KEY = "userDataCollectionNoticeAcknowledged";
    private static final String USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_AT_KEY = "userDataCollectionNoticeAcknowledgedAt";
    private static final String USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_BY_KEY = "userDataCollectionNoticeAcknowledgedBy";

    private final List<StateChangeHandler> m_listeners = Lists.newArrayList();
    private final CmProperties propertiesCache;

    public interface StateChangeHandler {
        void onIsEnabledChanged(boolean isEnabled);
    }

    public StateManager(ConfigurationManagerService cm) {
        ConfigUpdateInfo configIdentifier = new ConfigUpdateInfo(PROPERTIES_CONFIG_NAME, "default");
        propertiesCache = new CmProperties(cm, configIdentifier);
    }

    public Boolean isEnabled() throws IOException {
        return (Boolean) propertiesCache.getProperty(ENABLED_KEY);
    }

    public void setEnabled(boolean enabled, String user) throws Exception {
        propertiesCache.setProperty(ENABLED_KEY, enabled);
        propertiesCache.setProperty(ACKNOWLEDGED_BY_KEY, user == null ? "" : user);
        propertiesCache.setProperty(ACKNOWLEDGED_AT_KEY, new Date().toString());

        for (StateChangeHandler listener : m_listeners) {
            listener.onIsEnabledChanged(enabled);
        }
    }

    public Boolean isInitialNoticeAcknowledged() throws IOException {
        return (Boolean) propertiesCache.getProperty(INITIAL_NOTICE_ACKNOWLEDGED_KEY);
    }

    public void setInitialNoticeAcknowledged(boolean status, String user) throws Exception {
        propertiesCache.setProperty(INITIAL_NOTICE_ACKNOWLEDGED_KEY, status);
        propertiesCache.setProperty(INITIAL_NOTICE_ACKNOWLEDGED_BY_KEY, user == null ? "" : user);
        propertiesCache.setProperty(INITIAL_NOTICE_ACKNOWLEDGED_AT_KEY, new Date().toString());
    }

    public Boolean isUserDataCollectionNoticeAcknowledged() throws IOException {
        return (Boolean) propertiesCache.getProperty(USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_KEY);
    }

    public void setUserDataCollectionNoticeAcknowledged(boolean status, String user) throws Exception {
        propertiesCache.setProperty(USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_KEY, status);
        propertiesCache.setProperty(USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_BY_KEY, user == null ? "" : user);
        propertiesCache.setProperty(USER_DATA_COLLECTION_NOTICE_ACKNOWLEDGED_AT_KEY, new Date().toString());
    }

    public Boolean isUserDataCollectionOptedIn() throws IOException {
        return (Boolean) propertiesCache.getProperty(USER_DATA_COLLECTION_OPTED_IN_KEY);
    }

    public void setUserDataCollectionOptedIn(boolean status) throws Exception {
        propertiesCache.setProperty(USER_DATA_COLLECTION_OPTED_IN_KEY, status);
    }

    public String getOrGenerateSystemId() throws IOException {
        String systemId = (String) propertiesCache.getProperty(SYSTEM_ID_KEY);

        if (systemId == null) {
            LOG.debug("No existing system id was found. Generating a new system id.");
            systemId = UUID.randomUUID().toString();
            propertiesCache.setProperty(SYSTEM_ID_KEY, systemId);
        }
        return systemId;
    }

    public String getAndRegenerateSystemId() throws IOException {
        String systemId = UUID.randomUUID().toString();
        propertiesCache.setProperty(SYSTEM_ID_KEY, systemId);
        return systemId;
    }

    public void onIsEnabledChanged(StateChangeHandler callback) {
        m_listeners.add(Objects.requireNonNull(callback));
    }
}
