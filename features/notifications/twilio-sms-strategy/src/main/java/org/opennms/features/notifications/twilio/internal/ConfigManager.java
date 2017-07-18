/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.features.notifications.twilio.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.opennms.core.utils.PropertiesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Maintains state in cfg file:
 *   authSid=SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *   authToken=your_auth_token
 *   fromNumber=+1NXXNXXXXXX
 *   messagingServiceSid=MSXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *   validityPeriod=14400
 *
 * @author Jeff Gehlbach <jeffg@opennms.com>
 */
public class ConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);

    private static final String PROPERTIES_FILE_NAME = "org.opennms.features.notifications.twilio.cfg";
    private static final String AUTH_SID_KEY = "authSid";
    private static final String AUTH_TOKEN_KEY = "authToken";
    private static final String FROM_NUMBER_KEY = "fromNumber";
    private static final String MESSAGING_SERVICE_SID_KEY = "messagingServiceSid";
    private static final String VALIDITY_PERIOD_KEY = "validityPeriod";
    private static final String LAST_CHANGED_BY_KEY = "lastChangedBy";
    private static final String LAST_CHANGED_AT_KEY = "lastChangedAt";
    
    private static final Integer DEFAULT_VALIDITY_PERIOD_VALUE = 14400;

    private final List<ConfigChangeHandler> m_listeners = Lists.newArrayList();
    private final PropertiesCache m_propertiesCache = new PropertiesCache();
    private final File m_propertiesFile;

    public static interface ConfigChangeHandler {
        void onConfigChanged(Properties configProperties);
    }

    public ConfigManager() {
        String opennmsHomeStr = System.getProperty("opennms.home");
        if (opennmsHomeStr == null) {
            // Use working directory
            opennmsHomeStr = "";
        }
        m_propertiesFile = Paths.get(opennmsHomeStr, "etc", PROPERTIES_FILE_NAME).toFile();
    }

    public String getAuthSid() throws IOException {
        final String authSid = m_propertiesCache.getProperty(m_propertiesFile, AUTH_SID_KEY);
        return authSid == null ? null : authSid;
    }

    public void setAuthSid(String authSid, String user) throws Exception {
        m_propertiesCache.setProperty(m_propertiesFile, AUTH_SID_KEY, authSid);
        updateLastChanged(user);
        notifyListeners();
        LOG.debug("Auth SID set to {} by user {}", authSid, user);
    }
    
    public String getAuthToken() throws IOException {
    	final String authToken = m_propertiesCache.getProperty(m_propertiesFile, AUTH_TOKEN_KEY);
    	return authToken == null ? null : authToken;
    }
    
    public void setAuthToken(String authToken, String user) throws Exception {
        m_propertiesCache.setProperty(m_propertiesFile, AUTH_TOKEN_KEY, authToken);
        updateLastChanged(user);
        notifyListeners();
        LOG.debug("Auth token set by user {}", user);
    }
    
    public String getFromNumber() throws IOException {
    	final String fromNumber = m_propertiesCache.getProperty(m_propertiesFile, FROM_NUMBER_KEY);
    	return fromNumber == null ? null : fromNumber;
    }
    
    public void setFromNumber(String fromNumber, String user) throws Exception {
        m_propertiesCache.setProperty(m_propertiesFile, FROM_NUMBER_KEY, fromNumber);
        updateLastChanged(user);
        notifyListeners();
        LOG.debug("From number set to {} by user {}", fromNumber, user);
    }
    
    public Integer getValidityPeriod() throws IOException {
    	final String vpString = m_propertiesCache.getProperty(m_propertiesFile, VALIDITY_PERIOD_KEY);
    	Integer validityPeriod = null;
    	try {
    		validityPeriod = Integer.valueOf(vpString);
    	} catch (NumberFormatException nfe) {
    		LOG.warn("Configured validity period '{}' is not a valid integer. Using default value {} instead.", vpString, DEFAULT_VALIDITY_PERIOD_VALUE);
    	}
    	return validityPeriod == null ? DEFAULT_VALIDITY_PERIOD_VALUE : validityPeriod;
    }
    
    public String getMessagingServiceSid() throws IOException {
    	final String msgSvcSid = m_propertiesCache.getProperty(m_propertiesFile, MESSAGING_SERVICE_SID_KEY);
    	return msgSvcSid == null ? null : msgSvcSid;
    }
    
    public void setMessagingServiceSid(String msgSvcSid, String user) throws Exception {
    	m_propertiesCache.setProperty(m_propertiesFile, MESSAGING_SERVICE_SID_KEY, msgSvcSid);
    	updateLastChanged(user);
    	notifyListeners();
        LOG.debug("Messaging Service SID set to {} by user {}", msgSvcSid, user);
    }
    
    private void updateLastChanged(String user) throws Exception {
    	m_propertiesCache.setProperty(m_propertiesFile, LAST_CHANGED_BY_KEY, user == null ? "" : user);
    	m_propertiesCache.setProperty(m_propertiesFile, LAST_CHANGED_AT_KEY, new Date().toString());
    }
    
    private void notifyListeners() throws IOException {
    	for (ConfigChangeHandler listener : m_listeners) {
    		listener.onConfigChanged(m_propertiesCache.getProperties(m_propertiesFile));
    	}
    }

    public void onIsEnabledChanged(ConfigChangeHandler callback) {
        m_listeners.add(Objects.requireNonNull(callback));
    }
}
