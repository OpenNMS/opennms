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

package org.opennms.features.geocoder.nominatim;

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

public class NominatimConfiguration extends GeocoderConfiguration {

    public static final String EMAIL_KEY = "email";
    public static final String REFERER_KEY = "referer";
    public static final String USER_AGENT_KEY = "userAgent";
    public static final String USE_SYSTEM_PROXY_KEY = "useSystemProxy";
    public static final String URL_KEY = "url";
    public static final String ACCEPT_USAGE_TERMS_KEY = "acceptUsageTerms";
    private String referer;
    private String urlTemplate;
    private String userAgent;
    private String emailAddress;
    private boolean useSystemProxy;
    private boolean acceptUsageTerms;

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setUseSystemProxy(boolean useSystemProxy) {
        this.useSystemProxy = useSystemProxy;
    }

    public void setAcceptUsageTerms(boolean acceptUsageTerms) {
        this.acceptUsageTerms = acceptUsageTerms;
    }

    public String getReferer() {
        return referer;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isUseSystemProxy() {
        return useSystemProxy;
    }

    public boolean isAcceptUsageTerms() {
        return acceptUsageTerms;
    }

    @Override
    public void validate() throws GeocoderConfigurationException {
        if (!acceptUsageTerms) {
            throw new GeocoderConfigurationException(ACCEPT_USAGE_TERMS_KEY, "Please accept the usage terms");
        }
        if (Strings.isNullOrEmpty(userAgent) && Strings.isNullOrEmpty(referer)) {
            throw new GeocoderConfigurationException(USER_AGENT_KEY, "Either 'User Agent' or 'Referer' must be provided");
        }
        if (Strings.isNullOrEmpty(emailAddress)) {
            throw new GeocoderConfigurationException(EMAIL_KEY, "Please provide a valid email address");
        }
        if (Strings.isNullOrEmpty(urlTemplate)) {
            throw new GeocoderConfigurationException(URL_KEY, PROVIDE_A_VALUE_TEXT);
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
        properties.put(EMAIL_KEY, emailAddress);
        properties.put(REFERER_KEY, referer);
        properties.put(USER_AGENT_KEY, userAgent);
        properties.put(USE_SYSTEM_PROXY_KEY, useSystemProxy);
        properties.put(URL_KEY, urlTemplate);
        properties.put(ACCEPT_USAGE_TERMS_KEY, acceptUsageTerms);
        return properties;
    }

    public static NominatimConfiguration fromMap(Map<String, Object> properties) {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setEmailAddress(getValue(properties, EMAIL_KEY, null));
        configuration.setReferer(getValue(properties, REFERER_KEY, null));
        configuration.setUserAgent(getValue(properties, USER_AGENT_KEY, null));
        configuration.setUseSystemProxy(getBoolean(properties, USE_SYSTEM_PROXY_KEY, false));
        configuration.setUrlTemplate(getValue(properties, URL_KEY, null));
        configuration.setAcceptUsageTerms(getBoolean(properties, ACCEPT_USAGE_TERMS_KEY, false));
        return configuration;
    }
}
