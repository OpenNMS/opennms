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