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

package org.opennms.features.geocoder.mapquest;

import static org.opennms.features.geocoder.ConfigurationUtils.PROVIDE_A_VALUE_TEXT;
import static org.opennms.features.geocoder.ConfigurationUtils.URL_NOT_VALID_TEMPLATE;
import static org.opennms.features.geocoder.ConfigurationUtils.getBoolean;
import static org.opennms.features.geocoder.ConfigurationUtils.getValue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.geocoder.GeocoderConfiguration;
import org.opennms.features.geocoder.GeocoderConfigurationException;

import com.google.common.base.Strings;

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
        if (Strings.isNullOrEmpty(urlTemplate)) {
            throw new GeocoderConfigurationException(URL_KEY, PROVIDE_A_VALUE_TEXT);
        }
        if (Strings.isNullOrEmpty(apiKey)) {
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
}
