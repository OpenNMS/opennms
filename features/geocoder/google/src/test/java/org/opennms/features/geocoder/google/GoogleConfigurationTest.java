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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.geocoder.GeocoderConfigurationException;

public class GoogleConfigurationTest {

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailWhenEmpty() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.validate();
    }

    @Test()
    public void expectToPassIfApiKeyIsSet() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setApiKey("XXX");
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailWithMissingClientId() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setUseEnterpriseCredentials(true);
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailWithMissingSignature() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setUseEnterpriseCredentials(true);
        configuration.setClientId("clientId");
        configuration.validate();
    }

    @Test()
    public void expectToPassIfClientIdAndSignatureAreProvided() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setUseEnterpriseCredentials(true);
        configuration.setClientId("clientId");
        configuration.setSignature("signature");
        configuration.validate();
    }

    @Test(expected = GeocoderConfigurationException.class)
    public void expectToFailIfTimeoutSmallerThanZero() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setApiKey("XXX");
        configuration.setTimeout(-100);
        configuration.validate();
    }

    @Test
    public void expectToPassIfTimeoutIsZero() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setApiKey("XXX");
        configuration.setTimeout(0);
        configuration.validate();
    }

    @Test
    public void expectToPassIfTimeoutIsGreaterThanZero() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setApiKey("XXX");
        configuration.setTimeout(100);
        configuration.validate();
    }

    @Test
    public void verifyConfigurationCreation() {
        final GoogleConfiguration configuration = new GoogleConfiguration();
        configuration.setUseSystemProxy(true);
        configuration.setUseEnterpriseCredentials(true);
        configuration.setClientId("clientId");
        configuration.setSignature("signature");
        configuration.setApiKey("apiKey");
        configuration.setTimeout(100);

        final Map<String, Object> properties = configuration.asMap();
        Assert.assertEquals("apiKey", properties.get(GoogleConfiguration.API_KEY_KEY));
        Assert.assertEquals("clientId", properties.get(GoogleConfiguration.CLIENT_ID_KEY));
        Assert.assertEquals("signature", properties.get(GoogleConfiguration.SIGNATURE_KEY));
        Assert.assertEquals(true, properties.get(GoogleConfiguration.USE_SYSTEM_PROXY_KEY));
        Assert.assertEquals(true, properties.get(GoogleConfiguration.USE_ENTERPRISE_CREDENTIALS_KEY));
        Assert.assertEquals(100, properties.get(GoogleConfiguration.TIMEOUT_KEY));

        final GoogleConfiguration clonedConfiguration = GoogleConfiguration.fromMap(properties);
        Assert.assertEquals(configuration, clonedConfiguration);
    }

}