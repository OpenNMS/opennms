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
package org.opennms.features.geocoder.google;

import static org.opennms.features.geocoder.ConfigurationUtils.PROVIDE_A_VALUE_TEXT;
import static org.opennms.features.geocoder.ConfigurationUtils.getBoolean;
import static org.opennms.features.geocoder.ConfigurationUtils.getInteger;
import static org.opennms.features.geocoder.ConfigurationUtils.getValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.StringUtils;
import org.opennms.features.geocoder.GeocoderConfiguration;
import org.opennms.features.geocoder.GeocoderConfigurationException;

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
            if (StringUtils.isEmpty(clientId)) {
                throw new GeocoderConfigurationException(CLIENT_ID_KEY, PROVIDE_A_VALUE_TEXT);
            }
            if (StringUtils.isEmpty(signature)) {
                throw new GeocoderConfigurationException(SIGNATURE_KEY, PROVIDE_A_VALUE_TEXT);
            }
        } else if (StringUtils.isEmpty(apiKey)) {
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

    @Override
	public int hashCode() {
		return 31
				* super.hashCode()
				+ Objects.hash(apiKey, clientId, signature, timeout, useEnterpriseCredentials, useSystemProxy);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof GoogleConfiguration)) {
			return false;
		}
		final GoogleConfiguration that = (GoogleConfiguration) obj;
		return Objects.equals(this.apiKey, that.apiKey)
				&& Objects.equals(this.clientId, that.clientId)
				&& Objects.equals(this.signature, that.signature)
				&& this.timeout == that.timeout
				&& this.useEnterpriseCredentials == that.useEnterpriseCredentials
				&& this.useSystemProxy == that.useSystemProxy;
	}
}
