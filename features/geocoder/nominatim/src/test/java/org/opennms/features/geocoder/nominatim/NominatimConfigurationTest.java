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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.geocoder.GeocoderConfigurationException;

public class NominatimConfigurationTest {

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailIfUrlNotValid() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setAcceptUsageTerms(true);
        configuration.setEmailAddress("ulf@opennms.org");
        configuration.setReferer("dummy referer");
        configuration.setUserAgent("dummy agent");
        configuration.setUrlTemplate("some url");
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailIfNotAccepting() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setEmailAddress("ulf@opennms.org");
        configuration.setReferer("dummy referer");
        configuration.setUserAgent("dummy agent");
        configuration.setUrlTemplate("https://www.opennms.org");
        configuration.validate();
    }

    @Test
    public void expectToPassIfUserAgentIsSet() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setAcceptUsageTerms(true);
        configuration.setEmailAddress("ulf@opennms.org");
        configuration.setUserAgent("user Agent");
        configuration.setUrlTemplate("https://www.opennms.org");
        configuration.validate();
    }

    @Test
    public void expectToPassIfRefererIsSet() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setAcceptUsageTerms(true);
        configuration.setEmailAddress("test@test.de");
        configuration.setReferer("Dummy Referer");
        configuration.setUrlTemplate("https://www.opennms.org");
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailIfUrlNotSet() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setAcceptUsageTerms(true);
        configuration.setEmailAddress("test@test.de");
        configuration.setReferer("Dummy Referer");
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailIfEmpty() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setAcceptUsageTerms(true);
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailIfEmailIsEmpty() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setAcceptUsageTerms(true);
        configuration.setUserAgent("user Agent");
        configuration.setUrlTemplate("https://www.opennms.org");
        configuration.validate();
    }

    @Test
    public void verifyConfigurationCreation() {
        final NominatimConfiguration configuration = new NominatimConfiguration();
        configuration.setUseSystemProxy(true);
        configuration.setAcceptUsageTerms(true);
        configuration.setUrlTemplate("http://nominatim.opennms.org");
        configuration.setUserAgent("User Agent");
        configuration.setReferer("Referer");
        configuration.setEmailAddress("ulf@opennms.org");

        final Map<String, Object> properties = configuration.asMap();
        Assert.assertEquals(true, properties.get(NominatimConfiguration.USE_SYSTEM_PROXY_KEY));
        Assert.assertEquals(true, properties.get(NominatimConfiguration.ACCEPT_USAGE_TERMS_KEY));
        Assert.assertEquals("http://nominatim.opennms.org", properties.get(NominatimConfiguration.URL_KEY));
        Assert.assertEquals("User Agent", properties.get(NominatimConfiguration.USER_AGENT_KEY));
        Assert.assertEquals("Referer", properties.get(NominatimConfiguration.REFERER_KEY));
        Assert.assertEquals("ulf@opennms.org", properties.get(NominatimConfiguration.EMAIL_KEY));

        final NominatimConfiguration clonedConfiguration = NominatimConfiguration.fromMap(properties);
        Assert.assertEquals(configuration, clonedConfiguration);
    }
}