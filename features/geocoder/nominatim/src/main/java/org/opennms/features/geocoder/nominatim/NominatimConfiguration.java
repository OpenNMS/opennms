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
package org.opennms.features.geocoder.nominatim;

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
        if (StringUtils.isEmpty(userAgent) && StringUtils.isEmpty(referer)) {
            throw new GeocoderConfigurationException(USER_AGENT_KEY, "Either 'User Agent' or 'Referer' must be provided");
        }
        if (StringUtils.isEmpty(emailAddress)) {
            throw new GeocoderConfigurationException(EMAIL_KEY, "Please provide a valid email address");
        }
        if (StringUtils.isEmpty(urlTemplate)) {
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

    @Override
	public int hashCode() {
		return 31
				* super.hashCode()
				+ Objects.hash(acceptUsageTerms, emailAddress, referer, urlTemplate, useSystemProxy, userAgent);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof NominatimConfiguration)) {
			return false;
		}
		final NominatimConfiguration that = (NominatimConfiguration) obj;
		return acceptUsageTerms == that.acceptUsageTerms && Objects.equals(emailAddress, that.emailAddress)
				&& Objects.equals(referer, that.referer) && Objects.equals(urlTemplate, that.urlTemplate)
				&& useSystemProxy == that.useSystemProxy && Objects.equals(userAgent, that.userAgent);
	}
}
