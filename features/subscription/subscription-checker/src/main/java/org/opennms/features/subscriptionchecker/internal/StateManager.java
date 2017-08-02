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

package org.opennms.features.subscriptionchecker.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.opennms.core.utils.PropertiesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maintains state in cfg file:
 */
public class StateManager {
    private static final Logger LOG = LoggerFactory.getLogger(StateManager.class);

    private static final String PROPERTIES_FILE_NAME = "org.opennms.features.subscriptionchecker.cfg";
    private static final String ENABLED_KEY = "org.opennms.subscription.enabled";
    private static final String SUBSCRIPTION_ARTIFACT_ID_KEY = "org.opennms.subscription.artifactid";
    private static final String SUBSCRIPTION_GROUP_ID_KEY = "org.opennms.subscription.groupid";
    private static final String SUBSCRIPTION_VERSION_KEY = "org.opennms.subscription.version";
    private static final String SUBSCRIPTION_ADMIN_MESSAGE_KEY = "org.opennms.subscription.adminmessage";
    private static final String SUBSCRIPTION_USER_MESSAGE_KEY = "org.opennms.subscription.usermessage";

    private final PropertiesCache m_propertiesCache = new PropertiesCache();
    private final File m_propertiesFile;

    public StateManager() {
        String opennmsHomeStr = System.getProperty("opennms.home");
        if (opennmsHomeStr == null) {
            // Use working directory
            opennmsHomeStr = "";
        }
        m_propertiesFile = Paths.get(opennmsHomeStr, "etc", PROPERTIES_FILE_NAME).toFile();
    }

    public boolean isEnabled() throws IOException {
        final String enabled = m_propertiesCache.getProperty(m_propertiesFile, ENABLED_KEY);
        return enabled == null ? false : Boolean.valueOf(enabled);
    }
    
    public void setEnabled(boolean enabled) throws IOException {
		m_propertiesCache.setProperty(m_propertiesFile, ENABLED_KEY, Boolean.toString(enabled));
	}
    
    public String getSubscriptionArtifactId() throws IOException {
		String artifactId=m_propertiesCache.getProperty(m_propertiesFile, SUBSCRIPTION_ARTIFACT_ID_KEY);
		return (artifactId!=null) ? artifactId : SUBSCRIPTION_ARTIFACT_ID_KEY+ " not set in properties";
	}

	public void setSubscriptionArtifactId(String subscriptionArtifactId) throws IOException {
		m_propertiesCache.setProperty(m_propertiesFile, SUBSCRIPTION_ARTIFACT_ID_KEY, subscriptionArtifactId);
	}

	public String getSubscriptionGroupId() throws IOException {
		String subscriptionGroupId = m_propertiesCache.getProperty(m_propertiesFile, SUBSCRIPTION_GROUP_ID_KEY);
		return (subscriptionGroupId!=null) ? subscriptionGroupId : SUBSCRIPTION_GROUP_ID_KEY+ " not set in properties";
	}

	public void setSubscriptionGroupId(String subscriptionGroupId)throws IOException {
		m_propertiesCache.setProperty(m_propertiesFile, SUBSCRIPTION_GROUP_ID_KEY, subscriptionGroupId);
	}

	public String getSubscriptionVersion() throws IOException {
		String subscriptionVersion = m_propertiesCache.getProperty(m_propertiesFile, SUBSCRIPTION_VERSION_KEY);
		return (subscriptionVersion!=null) ? subscriptionVersion : SUBSCRIPTION_VERSION_KEY+ " not set in properties";
	}

	public void setSubscriptionVersion(String subscriptionVersion) throws IOException {
		m_propertiesCache.setProperty(m_propertiesFile, SUBSCRIPTION_VERSION_KEY, subscriptionVersion);
	}
	
	public String getAdminMessage() throws IOException {
		String adminMessage = m_propertiesCache.getProperty(m_propertiesFile, SUBSCRIPTION_ADMIN_MESSAGE_KEY);
		return (adminMessage!=null) ? adminMessage : SUBSCRIPTION_ADMIN_MESSAGE_KEY+ " not set in properties";
	}

	public void setAdminMessage(String adminMessage) throws IOException {
		m_propertiesCache.setProperty(m_propertiesFile, SUBSCRIPTION_ADMIN_MESSAGE_KEY, adminMessage);
	}

	public String getUserMessage() throws IOException {
		String userMessage = m_propertiesCache.getProperty(m_propertiesFile, SUBSCRIPTION_USER_MESSAGE_KEY);
		return (userMessage!=null) ? userMessage : SUBSCRIPTION_USER_MESSAGE_KEY+ " not set in properties";
	}

	public void setUserMessage(String userMessage) throws IOException {
		m_propertiesCache.setProperty(m_propertiesFile, SUBSCRIPTION_USER_MESSAGE_KEY, userMessage);
	}

}
