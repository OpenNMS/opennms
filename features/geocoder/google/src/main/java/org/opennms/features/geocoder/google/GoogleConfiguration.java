/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.geocoder.google;

import static org.opennms.features.geocoder.ConfigurationUtils.PROVIDE_A_VALUE_TEXT;
import static org.opennms.features.geocoder.ConfigurationUtils.getBoolean;
import static org.opennms.features.geocoder.ConfigurationUtils.getInteger;
import static org.opennms.features.geocoder.ConfigurationUtils.getValue;

import java.util.HashMap;
import java.util.Map;

import org.opennms.features.geocoder.GeocoderConfiguration;
import org.opennms.features.geocoder.GeocoderConfigurationException;

import com.google.common.base.Strings;

public class GoogleConfiguration extends GeocoderConfiguration {

    static final String TIMEOUT_KEY = "timeout";
    static final String CLIENT_ID_KEY = "clientId";
    static final String SIGNATURE_KEY = "signature";
    static final String USE_SYSTEM_PROXY_KEY = "useSystemProxy";
    static final String USE_ENTERPRISE_CREDENTIALS_KEY = "useEnterpriseCredentials";
    static final String API_KEY_KEY = "apiKey";

    private int timeout; // in ms
    private boolean useEnterpriseCredentials;
    private boolean useSystemProxy;
    private String clientId;
    private String signature;
    private String apiKey;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isUseEnterpriseCredentials() {
        return useEnterpriseCredentials;
    }

    public void setUseEnterpriseCredentials(boolean useEnterpriseCredentials) {
        this.useEnterpriseCredentials = useEnterpriseCredentials;
    }

    public boolean isUseSystemProxy() {
        return useSystemProxy;
    }

    public void setUseSystemProxy(boolean useSystemProxy) {
        this.useSystemProxy = useSystemProxy;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void validate() throws GeocoderConfigurationException {
        if (isUseEnterpriseCredentials()) {
            if (Strings.isNullOrEmpty(clientId)) {
                throw new GeocoderConfigurationException(CLIENT_ID_KEY, PROVIDE_A_VALUE_TEXT);
            }
            if (Strings.isNullOrEmpty(signature)) {
                throw new GeocoderConfigurationException(SIGNATURE_KEY, PROVIDE_A_VALUE_TEXT);
            }
        } else if (Strings.isNullOrEmpty(apiKey)) {
            throw new GeocoderConfigurationException(API_KEY_KEY, PROVIDE_A_VALUE_TEXT);
        }
        if (timeout < 0) {
            throw new GeocoderConfigurationException(TIMEOUT_KEY, "The provided value must be >= 0");
        }
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(TIMEOUT_KEY, timeout);
        properties.put(USE_ENTERPRISE_CREDENTIALS_KEY, useEnterpriseCredentials);
        properties.put(CLIENT_ID_KEY, clientId);
        properties.put(SIGNATURE_KEY, signature);
        properties.put(API_KEY_KEY, apiKey);
        properties.put(USE_SYSTEM_PROXY_KEY, useSystemProxy);
        return properties;
    }

    public static GoogleConfiguration fromMap(Map<String, Object> properties) {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setTimeout(getInteger(properties, TIMEOUT_KEY, 0));
        configuration.setUseEnterpriseCredentials(getBoolean(properties, USE_ENTERPRISE_CREDENTIALS_KEY, false));
        configuration.setUseSystemProxy(getBoolean(properties, USE_SYSTEM_PROXY_KEY, false));
        configuration.setClientId(getValue(properties, CLIENT_ID_KEY, null));
        configuration.setSignature(getValue(properties, SIGNATURE_KEY, null));
        configuration.setApiKey(getValue(properties, API_KEY_KEY, null));
        return configuration;
    }
}
