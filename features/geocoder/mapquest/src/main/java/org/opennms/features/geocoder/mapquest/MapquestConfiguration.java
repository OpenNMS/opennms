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
package org.opennms.features.geocoder.mapquest;

import static org.opennms.features.geocoder.ConfigurationUtils.PROVIDE_A_VALUE_TEXT;
import static org.opennms.features.geocoder.ConfigurationUtils.URL_NOT_VALID_TEMPLATE;
import static org.opennms.features.geocoder.ConfigurationUtils.getBoolean;
import static org.opennms.features.geocoder.ConfigurationUtils.getValue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.StringUtils;
import org.opennms.features.geocoder.GeocoderConfiguration;
import org.opennms.features.geocoder.GeocoderConfigurationException;

public class MapquestConfiguration extends GeocoderConfiguration {

    static final String URL_KEY = "url";
    static final String API_KEY_KEY = "apiKey";
    static final String USE_SYSTEM_PROXY_KEY = "useSystemProxy";

    private String urlTemplate;
    private String apiKey;
    private boolean useSystemProxy;

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isUseSystemProxy() {
        return useSystemProxy;
    }

    public void setUseSystemProxy(boolean useSystemProxy) {
        this.useSystemProxy = useSystemProxy;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void validate() throws GeocoderConfigurationException {
        if (StringUtils.isEmpty(urlTemplate)) {
            throw new GeocoderConfigurationException(URL_KEY, PROVIDE_A_VALUE_TEXT);
        }
        if (StringUtils.isEmpty(apiKey)) {
            throw new GeocoderConfigurationException(API_KEY_KEY, PROVIDE_A_VALUE_TEXT);
        }
        // Try parsing the URL
        try {
            new URL(urlTemplate);
        } catch (MalformedURLException e) {
            throw new GeocoderConfigurationException(URL_KEY, URL_NOT_VALID_TEMPLATE, urlTemplate, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(URL_KEY, urlTemplate);
        properties.put(API_KEY_KEY, apiKey);
        properties.put(USE_SYSTEM_PROXY_KEY, useSystemProxy);
        return properties;
    }

    public static MapquestConfiguration fromMap(Map<String, Object> properties) {
        final MapquestConfiguration configuration = new MapquestConfiguration();
        configuration.setUrlTemplate(getValue(properties, URL_KEY, null));
        configuration.setApiKey(getValue(properties, API_KEY_KEY, null));
        configuration.setUseSystemProxy(getBoolean(properties, USE_SYSTEM_PROXY_KEY, false));
        return configuration;
    }

	@Override
	public int hashCode() {
		return 31
				* super.hashCode()
				+ Objects.hash(apiKey, urlTemplate, useSystemProxy);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof MapquestConfiguration)) {
			return false;
		}
		final MapquestConfiguration that = (MapquestConfiguration) obj;
		return Objects.equals(this.apiKey, that.apiKey)
				&& Objects.equals(this.urlTemplate, that.urlTemplate)
				&& this.useSystemProxy == that.useSystemProxy;
	}

}
