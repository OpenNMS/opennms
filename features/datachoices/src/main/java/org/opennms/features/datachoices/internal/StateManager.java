/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
     * Whether a user opted-in to Product Update Enrollment.
     */
    private static final String PRODUCT_UPDATE_ENROLLMENT_OPTED_IN_KEY = "userDataCollectionOptedIn";

    /**
     * Whether the Product Update Enrollment notice was acknowledged.
     */
    private static final String PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_KEY = "userDataCollectionNoticeAcknowledged";
    private static final String PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_AT_KEY = "userDataCollectionNoticeAcknowledgedAt";
    private static final String PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_BY_KEY = "userDataCollectionNoticeAcknowledgedBy";

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

    public Boolean isProductUpdateEnrollmentNoticeAcknowledged() throws IOException {
        return (Boolean) propertiesCache.getProperty(PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_KEY);
    }

    public void setProductUpdateEnrollmentNoticeAcknowledged(boolean status, String user) throws Exception {
        propertiesCache.setProperty(PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_KEY, status);
        propertiesCache.setProperty(PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_BY_KEY, user == null ? "" : user);
        propertiesCache.setProperty(PRODUCT_UPDATE_ENROLLMENT_NOTICE_ACKNOWLEDGED_AT_KEY, new Date().toString());
    }

    public Boolean isProductUpdateEnrollmentOptedIn() throws IOException {
        return (Boolean) propertiesCache.getProperty(PRODUCT_UPDATE_ENROLLMENT_OPTED_IN_KEY);
    }

    public void setProductUpdateEnrollmentOptedIn(boolean status) throws Exception {
        propertiesCache.setProperty(PRODUCT_UPDATE_ENROLLMENT_OPTED_IN_KEY, status);
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
