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